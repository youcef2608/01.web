package com.example.ui.createcall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.HelpCall
import com.example.data.model.HelpCategory
import com.example.data.model.UrgencyLevel
import com.example.data.repository.GeminiRepository
import com.example.data.repository.HelpCallRepository
import com.example.data.repository.VolunteerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateCallUiState(
    val description: String = "",
    val suggestedTitle: String = "",
    val selectedCategory: HelpCategory = HelpCategory.OTHER,
    val selectedUrgency: UrgencyLevel = UrgencyLevel.MEDIUM,
    val locationName: String = "حي النور، الجزائر العاصمة",
    val latitude: Double = 36.7538,
    val longitude: Double = 3.0588,
    val isAnalyzingWithGemini: Boolean = false,
    val isPublishing: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class CreateCallViewModel(
    private val helpCallRepository: HelpCallRepository,
    private val volunteerRepository: VolunteerRepository,
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateCallUiState())
    val uiState: StateFlow<CreateCallUiState> = _uiState.asStateFlow()

    fun onDescriptionChanged(newText: String) {
        _uiState.value = _uiState.value.copy(description = newText)
    }

    fun onTitleChanged(newTitle: String) {
        _uiState.value = _uiState.value.copy(suggestedTitle = newTitle)
    }

    fun onCategorySelected(category: HelpCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun onUrgencySelected(urgency: UrgencyLevel) {
        _uiState.value = _uiState.value.copy(selectedUrgency = urgency)
    }

    fun onLocationChanged(name: String, lat: Double, lon: Double) {
        _uiState.value = _uiState.value.copy(
            locationName = name,
            latitude = lat,
            longitude = lon
        )
    }

    /**
     * استدعاء Gemini API لتحليل النص المدخل وتصنيف النداء ذكياً
     */
    fun analyzeWithGemini() {
        val currentText = _uiState.value.description.trim()
        if (currentText.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzingWithGemini = true, errorMessage = null)
            try {
                val classification = geminiRepository.classifyHelpCall(currentText)
                _uiState.value = _uiState.value.copy(
                    suggestedTitle = classification.suggestedTitle,
                    selectedCategory = classification.toHelpCategory(),
                    selectedUrgency = classification.toUrgencyLevel(),
                    isAnalyzingWithGemini = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAnalyzingWithGemini = false,
                    errorMessage = "تعذر التصنيف الذكي مؤقتاً، تم استخدام التصنيف الافتراضي."
                )
            }
        }
    }

    /**
     * نشر نداء المساعدة المجتمعي (غير المادي)
     */
    fun publishHelpCall(onSuccess: () -> Unit) {
        val state = _uiState.value
        val text = state.description.trim()
        if (text.isBlank()) {
            _uiState.value = state.copy(errorMessage = "يرجى كتابة تفاصيل نداء المساعدة.")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isPublishing = true, errorMessage = null)
            val user = volunteerRepository.currentVolunteer.value
            val title = state.suggestedTitle.ifBlank {
                if (text.length > 25) text.take(25) + "..." else text
            }

            val newCall = HelpCall(
                title = title,
                description = text,
                category = state.selectedCategory,
                urgency = state.selectedUrgency,
                latitude = state.latitude,
                longitude = state.longitude,
                locationName = state.locationName,
                requesterId = user.id,
                authorName = user.name,
                authorPhone = user.phoneNumber
            )

            helpCallRepository.createHelpCall(newCall)
            // زيادة عداد الاستفادة من التطوع في الملف الشخصي (التطوع العكسي)
            volunteerRepository.recordHelpReceived()

            _uiState.value = _uiState.value.copy(isPublishing = false, isSuccess = true)
            onSuccess()
        }
    }

    companion object {
        fun provideFactory(
            helpCallRepo: HelpCallRepository,
            volunteerRepo: VolunteerRepository,
            geminiRepo: GeminiRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CreateCallViewModel(helpCallRepo, volunteerRepo, geminiRepo) as T
            }
        }
    }
}
