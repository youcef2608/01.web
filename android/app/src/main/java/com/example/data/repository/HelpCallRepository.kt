package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.EchoActionDao
import com.example.data.local.EchoActionEntity
import com.example.data.local.HelpCallDao
import com.example.data.local.HelpCallEntity
import com.example.data.model.EchoAction
import com.example.data.model.HelpCall
import com.example.data.model.HelpCallStatus
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * مستودع نداءات المساعدة المجتمعية
 * يدعم التخزين المحلي الدائم الحقيقي (Room SQLite) مع المزامنة اللحظية في السحابة (Firestore Realtime)
 */
class HelpCallRepository(
    private val helpCallDao: HelpCallDao,
    private val echoActionDao: EchoActionDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    companion object {
        private const val TAG = "HelpCallRepository"
        private const val COLLECTION_CALLS = "help_calls"
        private const val COLLECTION_ECHOES = "echo_actions"
    }

    private val _helpCalls = MutableStateFlow<List<HelpCall>>(emptyList())
    val helpCalls: StateFlow<List<HelpCall>> = _helpCalls.asStateFlow()

    private val _echoActions = MutableStateFlow<List<EchoAction>>(emptyList())
    val echoActions: StateFlow<List<EchoAction>> = _echoActions.asStateFlow()

    private var firestore: FirebaseFirestore? = null
    private var callsListener: ListenerRegistration? = null
    private var echoesListener: ListenerRegistration? = null
    private var supabasePollingJob: kotlinx.coroutines.Job? = null

    init {
        // 1. مراقبة قاعدة البيانات المحلية الحقيقية (Room) كمصدر موحد للبيانات
        scope.launch {
            helpCallDao.getAllCalls().collect { entities ->
                _helpCalls.value = entities.map { it.toHelpCall() }
            }
        }

        scope.launch {
            echoActionDao.getAllEchoes().collect { entities ->
                _echoActions.value = entities.map { it.toEchoAction() }
            }
        }

        // 2. تهيئة الاتصال بـ Firebase Firestore إن توفر
        initFirebaseIfAvailable()
        listenToSupabaseCalls()
    }

    private fun listenToSupabaseCalls() {
        if (BuildConfig.SUPABASE_URL.isBlank() || BuildConfig.SUPABASE_PUBLISHABLE_KEY.isBlank()) return
        supabasePollingJob = scope.launch {
            val client = okhttp3.OkHttpClient()
            val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
            while (true) {
                try {
                    val request = okhttp3.Request.Builder()
                        .url("${BuildConfig.SUPABASE_URL}/rest/v1/help_calls?select=*&order=created_at.desc")
                        .header("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY)
                        .header("Accept", "application/json")
                        .build()
                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val body = response.body?.string().orEmpty()
                            val calls = json.decodeFromString<List<SupabaseHelpCall>>(body).map { it.toHelpCall() }
                            if (calls.isNotEmpty()) helpCallDao.insertAll(calls.map { HelpCallEntity.fromHelpCall(it) })
                        }
                    }
                } catch (error: Exception) {
                    Log.w(TAG, "Supabase help-call sync failed", error)
                }
                kotlinx.coroutines.delay(10_000)
            }
        }
    }

    @kotlinx.serialization.Serializable
    private data class SupabaseHelpCall(
        val id: String,
        val title: String,
        val description: String,
        val category: String = "other",
        val urgency: String = "medium",
        val status: String = "open",
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
        val location_name: String? = null,
        val requester_id: String = "",
        val author_name: String = "",
        val author_phone: String? = null,
        val volunteer_id: String? = null,
        val helper_name: String? = null,
        val created_at: String? = null,
        val completed_at: String? = null
    ) {
        fun toHelpCall() = HelpCall(
            id = id,
            title = title,
            description = description,
            category = com.example.data.model.HelpCategory.fromArabic(category),
            urgency = com.example.data.model.UrgencyLevel.fromArabic(urgency),
            status = when (status.lowercase()) {
                "in_progress" -> HelpCallStatus.IN_PROGRESS
                "completed" -> HelpCallStatus.COMPLETED
                "cancelled" -> HelpCallStatus.CANCELLED
                else -> HelpCallStatus.OPEN
            },
            latitude = latitude,
            longitude = longitude,
            locationName = location_name.orEmpty(),
            requesterId = requester_id,
            authorName = author_name,
            authorPhone = author_phone.orEmpty(),
            volunteerId = volunteer_id,
            helperName = helper_name,
            timestamp = created_at?.let { java.time.Instant.parse(it).toEpochMilli() } ?: System.currentTimeMillis(),
            completedAt = completed_at?.let { java.time.Instant.parse(it).toEpochMilli() }
        )
    }

    private fun initFirebaseIfAvailable() {
        try {
            if (FirebaseApp.getApps(com.example.AchdaApp.appContext).isNotEmpty()) {
                firestore = FirebaseFirestore.getInstance()
                listenToFirestoreCalls()
                listenToFirestoreEchoes()
                Log.d(TAG, "Firebase Firestore connected successfully.")
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Firebase not initialized or running in local database mode: ${e.message}")
        }
    }

    private fun listenToFirestoreCalls() {
        val db = firestore ?: return
        callsListener = db.collection(COLLECTION_CALLS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for calls", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val calls = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(HelpCall::class.java)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (calls.isNotEmpty()) {
                        scope.launch {
                            helpCallDao.insertAll(calls.map { HelpCallEntity.fromHelpCall(it) })
                        }
                    }
                }
            }
    }

    private fun listenToFirestoreEchoes() {
        val db = firestore ?: return
        echoesListener = db.collection(COLLECTION_ECHOES)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Listen failed for echoes", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val echoes = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(EchoAction::class.java)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (echoes.isNotEmpty()) {
                        scope.launch {
                            echoActionDao.insertAll(echoes.map { EchoActionEntity.fromEchoAction(it) })
                        }
                    }
                }
            }
    }

    /**
     * نشر نداء مساعدة حقيقي وتخزينه فوراً في قاعدة البيانات
     */
    suspend fun createHelpCall(call: HelpCall): HelpCall {
        val newId = call.id.ifBlank { UUID.randomUUID().toString() }
        val updatedCall = call.copy(id = newId, timestamp = System.currentTimeMillis())

        // تخزين حقيقي محلياً عبر Room
        helpCallDao.insertOrUpdate(HelpCallEntity.fromHelpCall(updatedCall))

        // المزامنة السحابية مع Firestore
        try {
            firestore?.collection(COLLECTION_CALLS)?.document(newId)?.set(updatedCall)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist to Firestore", e)
        }
        return updatedCall
    }

    /**
     * قبول مساعدة نداء معين وتحديث حالته في قاعدة البيانات
     */
    suspend fun acceptHelpCall(callId: String, volunteerId: String, helperName: String) {
        val targetEntity = helpCallDao.getCallById(callId) ?: return
        val currentCall = targetEntity.toHelpCall()

        val updated = currentCall.copy(
            status = HelpCallStatus.IN_PROGRESS,
            volunteerId = volunteerId,
            helperName = helperName
        )

        // حفظ التحديث في قاعدة بيانات Room
        helpCallDao.insertOrUpdate(HelpCallEntity.fromHelpCall(updated))

        try {
            firestore?.collection(COLLECTION_CALLS)?.document(callId)?.set(updated)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update call in Firestore", e)
        }
    }

    /**
     * طلب تأكيد الإتمام
     */
    suspend fun requestConfirmation(callId: String) {
        val targetEntity = helpCallDao.getCallById(callId) ?: return
        val updated = targetEntity.toHelpCall().copy(status = HelpCallStatus.PENDING_CONFIRMATION)

        helpCallDao.insertOrUpdate(HelpCallEntity.fromHelpCall(updated))

        try {
            firestore?.collection(COLLECTION_CALLS)?.document(callId)?.update("status", HelpCallStatus.PENDING_CONFIRMATION.name)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request confirmation in Firestore", e)
        }
    }

    /**
     * تأكيد إتمام المساعدة وحفظ أثر التطوع في قاعدة البيانات
     */
    suspend fun confirmCompletion(callId: String, confirmedByUserId: String, echoAction: EchoAction) {
        val targetEntity = helpCallDao.getCallById(callId) ?: return
        val target = targetEntity.toHelpCall()

        if (target.requesterId.isNotBlank() && target.requesterId != confirmedByUserId) {
            throw SecurityException("Only the original requester can confirm the completion.")
        }

        val updatedCall = target.copy(status = HelpCallStatus.COMPLETED, completedAt = System.currentTimeMillis())
        val newEcho = echoAction.copy(
            id = echoAction.id.ifBlank { UUID.randomUUID().toString() },
            timestamp = System.currentTimeMillis()
        )

        // حفظ في Room
        helpCallDao.insertOrUpdate(HelpCallEntity.fromHelpCall(updatedCall))
        echoActionDao.insert(EchoActionEntity.fromEchoAction(newEcho))

        // حفظ في Firestore
        try {
            firestore?.collection(COLLECTION_CALLS)?.document(callId)?.set(updatedCall)
            firestore?.collection(COLLECTION_ECHOES)?.document(newEcho.id)?.set(newEcho)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to confirm completion in Firestore", e)
        }
    }
}
