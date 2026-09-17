package com.example.data.repository

import android.util.Log
import com.example.data.local.VolunteerDao
import com.example.data.local.VolunteerEntity
import com.example.data.model.Volunteer
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * مستودع المتطوعين ولوحة الصدارة المجتمعية
 * مدعوم بقاعدة بيانات Room محلية مع مزامنة سحابية اختيارية عبر Firestore
 */
class VolunteerRepository(
    private val volunteerDao: VolunteerDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    companion object {
        private const val TAG = "VolunteerRepository"
        private const val CURRENT_USER_ID = "user_me"
        private const val COLLECTION_VOLUNTEERS = "volunteers"
    }

    private val _currentVolunteer = MutableStateFlow(
        Volunteer(
            id = CURRENT_USER_ID,
            name = "",
            phoneNumber = "",
            neighborhood = "",
            wilaya = "",
            baladiya = "",
            echoPoints = 0,
            impactStreak = 0,
            givenHelpsCount = 0,
            receivedHelpsCount = 0
        )
    )
    val currentVolunteer: StateFlow<Volunteer> = _currentVolunteer.asStateFlow()

    private val _neighborhoodLeaderboard = MutableStateFlow<List<Volunteer>>(emptyList())
    val neighborhoodLeaderboard: StateFlow<List<Volunteer>> = _neighborhoodLeaderboard.asStateFlow()

    private val _stateLeaderboard = MutableStateFlow<List<Volunteer>>(emptyList())
    val stateLeaderboard: StateFlow<List<Volunteer>> = _stateLeaderboard.asStateFlow()

    private val _monthlyLeaderboard = MutableStateFlow<List<Volunteer>>(emptyList())
    val monthlyLeaderboard: StateFlow<List<Volunteer>> = _monthlyLeaderboard.asStateFlow()

    private val _isUserLoggedIn = MutableStateFlow(false)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

    private val _isLocationSetupComplete = MutableStateFlow(false)
    val isLocationSetupComplete: StateFlow<Boolean> = _isLocationSetupComplete.asStateFlow()

    private var firestore: FirebaseFirestore? = null
    private var volunteersListener: ListenerRegistration? = null

    init {
        // تحميل بيانات المستخدم المسجلة محلياً في قاعدة البيانات الحقيقية (Room)
        scope.launch {
            val savedEntity = volunteerDao.getVolunteer(CURRENT_USER_ID)
            if (savedEntity != null) {
                val vol = savedEntity.toVolunteer()
                _currentVolunteer.value = vol
                _isUserLoggedIn.value = vol.phoneNumber.isNotBlank()
                checkLocationSetup()
            }

            // مراقبة كافة المتطوعين في قاعدة البيانات للوحات الصدارة الحقيقية
            volunteerDao.getAllVolunteers().collect { list ->
                val volunteers = list.map { it.toVolunteer() }
                refreshLeaderboards(volunteers)
            }
        }

        initFirebaseIfAvailable()
    }

    private fun initFirebaseIfAvailable() {
        try {
            if (FirebaseApp.getApps(com.example.AchdaApp.appContext).isNotEmpty()) {
                firestore = FirebaseFirestore.getInstance()
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser
                if (currentUser != null && !currentUser.phoneNumber.isNullOrBlank()) {
                    _isUserLoggedIn.value = true
                    loadProfileFromFirestore(currentUser.uid, currentUser.phoneNumber ?: "")
                }
                auth.addAuthStateListener { fbAuth ->
                    val user = fbAuth.currentUser
                    if (user != null) {
                        _isUserLoggedIn.value = true
                    } else if (_currentVolunteer.value.phoneNumber.isBlank()) {
                        _isUserLoggedIn.value = false
                    }
                }
                listenToFirestoreVolunteers()
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Firebase not available: ${e.message}")
        }
    }

    private fun loadProfileFromFirestore(userId: String, phone: String) {
        val db = firestore ?: return
        db.collection(COLLECTION_VOLUNTEERS).document(userId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val vol = snapshot.toObject(Volunteer::class.java)
                    if (vol != null) {
                        _currentVolunteer.value = vol
                        checkLocationSetup()
                        scope.launch {
                            volunteerDao.insertOrUpdate(VolunteerEntity.fromVolunteer(vol))
                        }
                    }
                } else {
                    val current = _currentVolunteer.value
                    if (current.phoneNumber.isBlank()) {
                        val updated = current.copy(id = userId, phoneNumber = phone)
                        persistVolunteer(updated)
                    }
                }
            }
    }

    private fun listenToFirestoreVolunteers() {
        val db = firestore ?: return
        volunteersListener = db.collection(COLLECTION_VOLUNTEERS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Failed listening to volunteers", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val remoteVolunteers = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Volunteer::class.java)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (remoteVolunteers.isNotEmpty()) {
                        scope.launch {
                            volunteerDao.insertAll(remoteVolunteers.map { VolunteerEntity.fromVolunteer(it) })
                        }
                    }
                }
            }
    }

    private fun checkLocationSetup() {
        val vol = _currentVolunteer.value
        _isLocationSetupComplete.value = vol.wilaya.isNotBlank() && vol.baladiya.isNotBlank()
    }

    /**
     * حفظ المتطوع محلياً في Room وسحابياً في Firestore
     */
    private fun persistVolunteer(vol: Volunteer) {
        _currentVolunteer.value = vol
        checkLocationSetup()
        scope.launch {
            volunteerDao.insertOrUpdate(VolunteerEntity.fromVolunteer(vol))
            try {
                val data = hashMapOf<String, Any>(
                    "id" to vol.id,
                    "name" to vol.name,
                    "phoneNumber" to vol.phoneNumber,
                    "registrationDate" to vol.registrationDate,
                    "echoPoints" to vol.echoPoints,
                    "wilaya" to vol.wilaya,
                    "baladiya" to vol.baladiya,
                    "neighborhood" to vol.neighborhood,
                    "avatarUrl" to vol.avatarUrl,
                    "impactStreak" to vol.impactStreak,
                    "givenHelpsCount" to vol.givenHelpsCount,
                    "receivedHelpsCount" to vol.receivedHelpsCount,
                    "recentStory" to vol.recentStory
                )
                firestore?.collection(COLLECTION_VOLUNTEERS)?.document(vol.id)?.set(data, SetOptions.merge())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist volunteer to Firestore", e)
            }
        }
    }

    /**
     * منح نقاط أصداء للمتطوع عند إتمام نداء مساعدة، وتحديث سلسلة الأثر والشارات
     */
    fun recordSuccessfulHelp(echoPoints: Int = 50, story: String) {
        val current = _currentVolunteer.value
        val newPoints = current.echoPoints + echoPoints
        val newGiven = current.givenHelpsCount + 1
        val newStreak = current.impactStreak + 1

        val updatedBadges = current.badges.map { badge ->
            when (badge.id) {
                "guardian" -> {
                    val p = (newGiven).coerceAtMost(badge.maxProgress)
                    badge.copy(progress = p, isUnlocked = p >= badge.maxProgress)
                }
                "hope_bringer" -> {
                    val p = (badge.progress + 1).coerceAtMost(badge.maxProgress)
                    badge.copy(progress = p, isUnlocked = p >= badge.maxProgress)
                }
                "community_pillar" -> {
                    val p = (badge.progress + 1).coerceAtMost(badge.maxProgress)
                    badge.copy(progress = p, isUnlocked = p >= badge.maxProgress)
                }
                else -> badge
            }
        }

        val updatedVolunteer = current.copy(
            echoPoints = newPoints,
            givenHelpsCount = newGiven,
            impactStreak = newStreak,
            badges = updatedBadges,
            recentStory = story
        )
        persistVolunteer(updatedVolunteer)
    }

    /**
     * تسجيل طلب مساعدة مستفاد منه (زيادة عداد المستفيد في التطوع العكسي)
     */
    fun recordHelpReceived() {
        val current = _currentVolunteer.value
        val updated = current.copy(receivedHelpsCount = current.receivedHelpsCount + 1)
        persistVolunteer(updated)
    }

    /**
     * تسجيل الدخول برقم الهاتف وحفظ المستخدم في قاعدة البيانات
     */
    fun loginWithPhone(phoneNumber: String, name: String) {
        val updated = _currentVolunteer.value.copy(
            phoneNumber = phoneNumber,
            name = name.ifBlank { "متطوع لمّة" }
        )
        _isUserLoggedIn.value = true
        persistVolunteer(updated)
    }

    /**
     * حفظ الملف الشخصي الكامل في قاعدة البيانات المحلية والسحابية
     */
    fun saveFullProfile(
        name: String,
        phoneNumber: String,
        wilaya: String,
        baladiya: String,
        neighborhood: String,
        echoPoints: Int? = null,
        avatarUrl: String? = null
    ) {
        val current = _currentVolunteer.value
        val updated = current.copy(
            name = name.ifBlank { current.name.ifBlank { "متطوع لمّة" } },
            phoneNumber = phoneNumber.ifBlank { current.phoneNumber },
            wilaya = wilaya.ifBlank { current.wilaya },
            baladiya = baladiya.ifBlank { current.baladiya },
            neighborhood = neighborhood.ifBlank { current.neighborhood },
            echoPoints = echoPoints ?: current.echoPoints,
            avatarUrl = avatarUrl ?: current.avatarUrl
        )
        _isUserLoggedIn.value = updated.phoneNumber.isNotBlank()
        persistVolunteer(updated)
    }

    /**
     * تعديل الاسم والصورة الشخصية
     */
    fun updateProfile(newName: String, avatarUrl: String? = null) {
        val current = _currentVolunteer.value
        val updated = current.copy(
            name = newName.ifBlank { current.name },
            avatarUrl = avatarUrl ?: current.avatarUrl
        )
        persistVolunteer(updated)
    }

    /**
     * تعديل الاسم فقط
     */
    fun updateProfileName(newName: String) {
        val current = _currentVolunteer.value
        val updated = current.copy(name = newName.ifBlank { current.name })
        persistVolunteer(updated)
    }

    /**
     * تحديث المنطقة الجغرافية (الولاية، البلدية، الحي) وحفظها في قاعدة البيانات
     */
    fun updateLocation(wilaya: String, baladiya: String, neighborhood: String) {
        val updated = _currentVolunteer.value.copy(
            wilaya = wilaya,
            baladiya = baladiya,
            neighborhood = neighborhood
        )
        persistVolunteer(updated)
    }

    fun logout() {
        try {
            if (FirebaseApp.getApps(com.example.AchdaApp.appContext).isNotEmpty()) {
                FirebaseAuth.getInstance().signOut()
            }
        } catch (_: Throwable) {}
        _isUserLoggedIn.value = false
        val cleared = Volunteer(id = CURRENT_USER_ID, name = "متطوع لمّة")
        _currentVolunteer.value = cleared
        _isLocationSetupComplete.value = false
        scope.launch {
            volunteerDao.insertOrUpdate(VolunteerEntity.fromVolunteer(cleared))
        }
    }

    private fun refreshLeaderboards(allVolunteers: List<Volunteer>) {
        val me = _currentVolunteer.value
        val combined = (allVolunteers.filter { it.id != me.id } + me).sortedByDescending { it.echoPoints }

        // صدارة الحي
        _neighborhoodLeaderboard.value = combined.filter { it.neighborhood.isNotBlank() && it.neighborhood == me.neighborhood }

        // صدارة الولاية
        _stateLeaderboard.value = combined.filter { it.wilaya.isNotBlank() && it.wilaya == me.wilaya }

        // صدارة الشهر
        _monthlyLeaderboard.value = combined
    }
}
