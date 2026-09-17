package com.example.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.AchdaApp
import com.example.data.model.AlgeriaLocations
import com.example.data.model.Volunteer
import com.example.data.repository.LocationRepository
import com.example.data.repository.VolunteerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SettingsUiState(
    val volunteer: Volunteer = Volunteer(),
    val notificationsEnabled: Boolean = true,
    val urgentOnlyNotifications: Boolean = false,
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val isEditProfileDialogOpen: Boolean = false,
    val isLocationDialogOpen: Boolean = false,
    val isLogoutDialogOpen: Boolean = false,
    val isPrivacyDialogOpen: Boolean = false,
    val isAboutDialogOpen: Boolean = false,
    val newNameInput: String = "",
    val selectedAvatar: String = "",
    val selectedWilaya: String = "",
    val selectedBaladiya: String = "",
    val selectedNeighborhood: String = "",
    val availableWilayas: List<String> = emptyList(),
    val availableBaladiyas: List<String> = emptyList()
)

class SettingsViewModel(
    private val volunteerRepository: VolunteerRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val prefs = AchdaApp.appContext.getSharedPreferences("achda_settings", Context.MODE_PRIVATE)

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notif_enabled", true))
    private val _urgentOnly = MutableStateFlow(prefs.getBoolean("urgent_only", false))
    private val _editState = MutableStateFlow(
        SettingsUiState(
            availableWilayas = AlgeriaLocations.wilayas
        )
    )

    val uiState: StateFlow<SettingsUiState> = combine(
        volunteerRepository.currentVolunteer,
        _notificationsEnabled,
        _urgentOnly,
        _editState
    ) { vol, notif, urgent, edit ->
        edit.copy(
            volunteer = vol,
            notificationsEnabled = notif,
            urgentOnlyNotifications = urgent
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState(availableWilayas = AlgeriaLocations.wilayas)
    )

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        prefs.edit().putBoolean("notif_enabled", enabled).apply()
    }

    fun toggleUrgentOnly(enabled: Boolean) {
        _urgentOnly.value = enabled
        prefs.edit().putBoolean("urgent_only", enabled).apply()
    }

    fun openEditNameDialog() {
        openEditProfileDialog()
    }

    fun openEditProfileDialog() {
        val currentVol = volunteerRepository.currentVolunteer.value
        _editState.value = _editState.value.copy(
            isEditProfileDialogOpen = true,
            newNameInput = currentVol.name,
            selectedAvatar = currentVol.avatarUrl,
            successMessage = null,
            errorMessage = null
        )
    }

    fun closeEditProfileDialog() {
        _editState.value = _editState.value.copy(isEditProfileDialogOpen = false)
    }

    fun closeEditNameDialog() {
        closeEditProfileDialog()
    }

    fun onNewNameChanged(name: String) {
        _editState.value = _editState.value.copy(newNameInput = name)
    }

    fun onAvatarSelected(avatarKey: String) {
        _editState.value = _editState.value.copy(selectedAvatar = avatarKey)
    }

    fun saveProfile() {
        val name = _editState.value.newNameInput.trim()
        if (name.isBlank()) {
            _editState.value = _editState.value.copy(errorMessage = "يرجى كتابة الاسم بشكل صحيح")
            return
        }
        val avatar = _editState.value.selectedAvatar
        volunteerRepository.updateProfile(name, avatar)
        _editState.value = _editState.value.copy(
            isEditProfileDialogOpen = false,
            successMessage = "تم تحديث الملف الشخصي والصورة بنجاح"
        )
    }

    fun saveName() {
        saveProfile()
    }

    fun openPrivacyDialog() {
        _editState.value = _editState.value.copy(isPrivacyDialogOpen = true)
    }

    fun closePrivacyDialog() {
        _editState.value = _editState.value.copy(isPrivacyDialogOpen = false)
    }

    fun openAboutDialog() {
        _editState.value = _editState.value.copy(isAboutDialogOpen = true)
    }

    fun closeAboutDialog() {
        _editState.value = _editState.value.copy(isAboutDialogOpen = false)
    }

    fun openLocationDialog() {
        val vol = volunteerRepository.currentVolunteer.value
        val initialWilaya = if (vol.wilaya.isNotBlank()) vol.wilaya else AlgeriaLocations.wilayas.first()
        val baladiyas = AlgeriaLocations.getBaladiyas(initialWilaya)
        _editState.value = _editState.value.copy(
            isLocationDialogOpen = true,
            selectedWilaya = initialWilaya,
            selectedBaladiya = if (vol.baladiya.isNotBlank()) vol.baladiya else (baladiyas.firstOrNull() ?: ""),
            selectedNeighborhood = vol.neighborhood,
            availableBaladiyas = baladiyas,
            successMessage = null,
            errorMessage = null
        )
    }

    fun closeLocationDialog() {
        _editState.value = _editState.value.copy(isLocationDialogOpen = false)
    }

    fun selectWilaya(wilaya: String) {
        val baladiyas = AlgeriaLocations.getBaladiyas(wilaya)
        _editState.value = _editState.value.copy(
            selectedWilaya = wilaya,
            selectedBaladiya = baladiyas.firstOrNull() ?: "",
            availableBaladiyas = baladiyas
        )
    }

    fun selectBaladiya(baladiya: String) {
        _editState.value = _editState.value.copy(selectedBaladiya = baladiya)
    }

    fun onNeighborhoodChanged(neighborhood: String) {
        _editState.value = _editState.value.copy(selectedNeighborhood = neighborhood)
    }

    fun saveLocation() {
        val state = _editState.value
        if (state.selectedWilaya.isBlank() || state.selectedBaladiya.isBlank()) {
            _editState.value = state.copy(errorMessage = "يرجى اختيار الولاية والبلدية")
            return
        }
        volunteerRepository.updateLocation(
            wilaya = state.selectedWilaya,
            baladiya = state.selectedBaladiya,
            neighborhood = state.selectedNeighborhood
        )
        _editState.value = _editState.value.copy(
            isLocationDialogOpen = false,
            successMessage = "تم حفظ منطقتك التطوعية بنجاح"
        )
    }

    fun openLogoutDialog() {
        _editState.value = _editState.value.copy(isLogoutDialogOpen = true)
    }

    fun closeLogoutDialog() {
        _editState.value = _editState.value.copy(isLogoutDialogOpen = false)
    }

    fun confirmLogout() {
        volunteerRepository.logout()
        _editState.value = _editState.value.copy(
            isLogoutDialogOpen = false,
            successMessage = "تم تسجيل الخروج"
        )
    }

    fun clearMessages() {
        _editState.value = _editState.value.copy(successMessage = null, errorMessage = null)
    }

    companion object Factory {
        fun provideFactory(
            volunteerRepo: VolunteerRepository,
            locationRepo: LocationRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(volunteerRepo, locationRepo) as T
            }
        }
    }
}
