package com.example.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.EchoAction
import com.example.data.model.HelpCall
import com.example.data.model.HelpCallStatus
import com.example.data.model.HelpCategory
import com.example.data.model.UrgencyLevel
import com.example.data.repository.GeminiRepository
import com.example.data.repository.HelpCallRepository
import com.example.data.repository.LocationRepository
import com.example.data.repository.VolunteerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FeedFilterCriteria(
    val selectedCategory: HelpCategory? = null,
    val selectedUrgency: UrgencyLevel? = null,
    val userLat: Double = 36.7538,
    val userLon: Double = 3.0588,
    val activeEchoNotice: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class FeedUiState(
    val calls: List<HelpCall> = emptyList(),
    val selectedCategory: HelpCategory? = null,
    val selectedUrgency: UrgencyLevel? = null,
    val userLatitude: Double = 36.7538,
    val userLongitude: Double = 3.0588,
    val isLoading: Boolean = false,
    val activeEchoNotice: String? = null,
    val errorMessage: String? = null,
    val currentUserId: String = ""
)

class FeedViewModel(
    private val helpCallRepository: HelpCallRepository,
    private val volunteerRepository: VolunteerRepository,
    private val geminiRepository: GeminiRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _filterCriteria = MutableStateFlow(FeedFilterCriteria())
    
    fun fetchUserLocation() {
        viewModelScope.launch {
            val loc = locationRepository.getCurrentLocation()
            if (loc != null) {
                updateUserLocation(loc.latitude, loc.longitude)
                _filterCriteria.value = _filterCriteria.value.copy(errorMessage = null)
            } else {
                _filterCriteria.value = _filterCriteria.value.copy(
                    errorMessage = "تعذر الحصول على الموقع الجغرافي. يرجى التأكد من تفعيل GPS."
                )
            }
        }
    }

    val uiState: StateFlow<FeedUiState> = combine(
        helpCallRepository.helpCalls,
        _filterCriteria,
        volunteerRepository.currentVolunteer
    ) { calls, criteria, user ->
        val filtered = calls.filter { call ->
            (criteria.selectedCategory == null || call.category == criteria.selectedCategory) &&
            (criteria.selectedUrgency == null || call.urgency == criteria.selectedUrgency)
        }.sortedWith(
            compareBy<HelpCall> { it.status == HelpCallStatus.COMPLETED }
                .thenByDescending { it.urgency == UrgencyLevel.URGENT }
                .thenBy { it.distanceTo(criteria.userLat, criteria.userLon) }
        )

        FeedUiState(
            calls = filtered,
            selectedCategory = criteria.selectedCategory,
            selectedUrgency = criteria.selectedUrgency,
            userLatitude = criteria.userLat,
            userLongitude = criteria.userLon,
            isLoading = criteria.isLoading,
            activeEchoNotice = criteria.activeEchoNotice,
            errorMessage = criteria.errorMessage,
            currentUserId = user.id
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FeedUiState()
    )

    fun selectCategory(category: HelpCategory?) {
        val current = _filterCriteria.value
        val newCategory = if (current.selectedCategory == category) null else category
        _filterCriteria.value = current.copy(selectedCategory = newCategory)
    }

    fun selectUrgency(urgency: UrgencyLevel?) {
        val current = _filterCriteria.value
        val newUrgency = if (current.selectedUrgency == urgency) null else urgency
        _filterCriteria.value = current.copy(selectedUrgency = newUrgency)
    }

    fun updateUserLocation(lat: Double, lon: Double) {
        _filterCriteria.value = _filterCriteria.value.copy(userLat = lat, userLon = lon)
    }

    /**
     * الضغط على زر "أنا سأساعد" لتغيير الحالة فورياً إلى "قيد التنفيذ"
     */
    fun volunteerToHelp(call: HelpCall) {
        val me = volunteerRepository.currentVolunteer.value
        viewModelScope.launch {
            _filterCriteria.value = _filterCriteria.value.copy(isLoading = true)
            helpCallRepository.acceptHelpCall(
                callId = call.id,
                volunteerId = me.id,
                helperName = me.name
            )
            _filterCriteria.value = _filterCriteria.value.copy(
                activeEchoNotice = "جزاك الله خيراً! أصبح نداء المساعدة قيد تنفيذك الآن.",
                isLoading = false
            )
        }
    }

    /**
     * المتطوع يضغط على زر أنه أتم المهمة ويطلب تأكيد صاحب النداء
     */
    fun requestConfirmation(call: HelpCall) {
        viewModelScope.launch {
            _filterCriteria.value = _filterCriteria.value.copy(isLoading = true)
            helpCallRepository.requestConfirmation(call.id)
            _filterCriteria.value = _filterCriteria.value.copy(
                activeEchoNotice = "تم إرسال طلب التأكيد إلى صاحب النداء بنجاح.",
                isLoading = false
            )
        }
    }

    /**
     * تأكيد إتمام المساعدة واستدعاء Gemini لتوليد جملة الأثر ومنح نقاط الأصداء (لصاحب النداء فقط)
     */
    fun confirmCompletion(call: HelpCall) {
        val me = volunteerRepository.currentVolunteer.value
        viewModelScope.launch {
            _filterCriteria.value = _filterCriteria.value.copy(isLoading = true)

            val helperName = call.helperName ?: "متطوع"

            val generatedStory = geminiRepository.generateImpactStory(
                helperName = helperName,
                beneficiaryName = me.name,
                category = call.category.labelArabic,
                helpCount = 1
            )

            val echoAction = EchoAction(
                helpCallId = call.id,
                helperId = call.volunteerId ?: "",
                helperName = helperName,
                beneficiaryName = me.name,
                category = call.category,
                generatedStory = generatedStory,
                echoPointsAwarded = 50
            )

            try {
                helpCallRepository.confirmCompletion(call.id, me.id, echoAction)
                volunteerRepository.recordHelpReceived()
                
                _filterCriteria.value = _filterCriteria.value.copy(
                    activeEchoNotice = "تم تأكيد المساعدة بنجاح ومنح المتطوع 50 صدى تكافلي.\n\"$generatedStory\"",
                    isLoading = false
                )
            } catch (e: SecurityException) {
                _filterCriteria.value = _filterCriteria.value.copy(
                    activeEchoNotice = "خطأ أمني: أنت لست صاحب النداء لتأكيد الإتمام.",
                    isLoading = false
                )
            }
        }
    }

    fun dismissNotice() {
        _filterCriteria.value = _filterCriteria.value.copy(activeEchoNotice = null)
    }

    companion object {
        fun provideFactory(
            helpCallRepo: HelpCallRepository,
            volunteerRepo: VolunteerRepository,
            geminiRepo: GeminiRepository,
            locationRepo: LocationRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FeedViewModel(helpCallRepo, volunteerRepo, geminiRepo, locationRepo) as T
            }
        }
    }
}
