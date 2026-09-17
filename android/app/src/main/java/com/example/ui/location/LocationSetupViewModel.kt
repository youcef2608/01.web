package com.example.ui.location

import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.AchdaApp
import com.example.data.repository.LocationRepository
import com.example.data.repository.VolunteerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class LocationSetupUiState(
    val wilayas: List<String> = listOf("الجزائر العاصمة", "وهران", "قسنطينة", "عنابة", "تلمسان"),
    val baladiyas: Map<String, List<String>> = mapOf(
        "الجزائر العاصمة" to listOf("سيدي امحمد", "القصبة", "الرويبة", "باب الوادي", "بوزريعة"),
        "وهران" to listOf("وهران", "بئر الجير", "السانية", "أرزيو"),
        "قسنطينة" to listOf("قسنطينة", "الخروب", "حامة بوزيان"),
        "عنابة" to listOf("عنابة", "البوني", "سيدي عمار"),
        "تلمسان" to listOf("تلمسان", "منصورة", "مغنية")
    ),
    val selectedWilaya: String = "",
    val selectedBaladiya: String = "",
    val neighborhood: String = "",
    val isAutoDetecting: Boolean = false,
    val autoDetectError: String? = null
)

class LocationSetupViewModel(
    private val locationRepository: LocationRepository,
    private val volunteerRepository: VolunteerRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LocationSetupUiState())
    val uiState: StateFlow<LocationSetupUiState> = _uiState.asStateFlow()

    fun onWilayaSelected(wilaya: String) {
        _uiState.value = _uiState.value.copy(
            selectedWilaya = wilaya,
            selectedBaladiya = "" // Reset baladiya when wilaya changes
        )
    }

    fun onBaladiyaSelected(baladiya: String) {
        _uiState.value = _uiState.value.copy(selectedBaladiya = baladiya)
    }

    fun onNeighborhoodChanged(neighborhood: String) {
        _uiState.value = _uiState.value.copy(neighborhood = neighborhood)
    }

    fun autoDetectLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAutoDetecting = true, autoDetectError = null)
            val location = locationRepository.getCurrentLocation()
            if (location != null) {
                try {
                    val geocoder = Geocoder(AchdaApp.appContext, java.util.Locale.forLanguageTag("ar-DZ"))
                    @Suppress("DEPRECATION")
                    val addresses = withContext(Dispatchers.IO) {
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    }
                    val address = addresses?.firstOrNull()
                    if (address != null) {
                        val detectedWilaya = address.adminArea ?: address.subAdminArea ?: ""
                        val detectedBaladiya = address.locality ?: address.subLocality ?: ""
                        val detectedNeighborhood = address.thoroughfare ?: address.subThoroughfare ?: address.featureName ?: ""

                        // Simple matching with our hardcoded list
                        val state = _uiState.value
                        val matchedWilaya = state.wilayas.find { it.contains(detectedWilaya) || detectedWilaya.contains(it) }
                            ?: (if (detectedWilaya.isNotBlank()) detectedWilaya else state.selectedWilaya)
                        
                        val matchedBaladiya = if (matchedWilaya.isNotBlank() && state.baladiyas.containsKey(matchedWilaya)) {
                             state.baladiyas[matchedWilaya]?.find { it.contains(detectedBaladiya) || detectedBaladiya.contains(it) }
                                 ?: (if (detectedBaladiya.isNotBlank()) detectedBaladiya else "")
                        } else detectedBaladiya

                        _uiState.value = state.copy(
                            selectedWilaya = matchedWilaya,
                            selectedBaladiya = matchedBaladiya,
                            neighborhood = detectedNeighborhood,
                            isAutoDetecting = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isAutoDetecting = false,
                            autoDetectError = "تعذر تحديد العنوان من الإحداثيات."
                        )
                    }
                } catch (e: Exception) {
                    Log.e("LocationSetup", "Geocoder error", e)
                    _uiState.value = _uiState.value.copy(
                        isAutoDetecting = false,
                        autoDetectError = "حدث خطأ أثناء الاتصال بخدمة الخرائط."
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isAutoDetecting = false,
                    autoDetectError = "تعذر الحصول على الموقع الجغرافي. تأكد من تفعيل الـ GPS."
                )
            }
        }
    }

    fun saveLocation() {
        val state = _uiState.value
        if (state.selectedWilaya.isNotBlank() && state.selectedBaladiya.isNotBlank()) {
            volunteerRepository.updateLocation(
                wilaya = state.selectedWilaya,
                baladiya = state.selectedBaladiya,
                neighborhood = state.neighborhood.ifBlank { state.selectedBaladiya }
            )
        }
    }

    companion object {
        fun provideFactory(
            locationRepo: LocationRepository,
            volunteerRepo: VolunteerRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LocationSetupViewModel(locationRepo, volunteerRepo) as T
            }
        }
    }
}
