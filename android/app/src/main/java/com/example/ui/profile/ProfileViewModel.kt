package com.example.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Badge
import com.example.data.model.EchoAction
import com.example.data.model.Volunteer
import com.example.data.repository.HelpCallRepository
import com.example.data.repository.VolunteerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ProfileDialogState(
    val isLoginDialogVisible: Boolean = false,
    val isOtpSent: Boolean = false,
    val phoneNumberInput: String = "+213",
    val nameInput: String = "",
    val otpInput: String = "",
    val isAuthLoading: Boolean = false,
    val authError: String? = null
)

data class ProfileUiState(
    val volunteer: Volunteer = Volunteer(),
    val echoHistory: List<EchoAction> = emptyList(),
    val isOtpSent: Boolean = false,
    val phoneNumberInput: String = "+213",
    val nameInput: String = "",
    val otpInput: String = "",
    val isLoginDialogVisible: Boolean = false,
    val isAuthLoading: Boolean = false,
    val authError: String? = null
)

class ProfileViewModel(
    private val volunteerRepository: VolunteerRepository,
    private val helpCallRepository: HelpCallRepository
) : ViewModel() {

    private val _dialogState = MutableStateFlow(ProfileDialogState())

    val uiState: StateFlow<ProfileUiState> = combine(
        volunteerRepository.currentVolunteer,
        helpCallRepository.echoActions,
        _dialogState
    ) { user, echoes, dialog ->
        val userEchoes = echoes.filter { it.helperId == user.id }
        ProfileUiState(
            volunteer = user,
            echoHistory = userEchoes,
            isLoginDialogVisible = dialog.isLoginDialogVisible,
            isOtpSent = dialog.isOtpSent,
            phoneNumberInput = dialog.phoneNumberInput,
            nameInput = dialog.nameInput,
            otpInput = dialog.otpInput,
            isAuthLoading = dialog.isAuthLoading,
            authError = dialog.authError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProfileUiState()
    )

    fun showLoginDialog() {
        _dialogState.value = _dialogState.value.copy(
            isLoginDialogVisible = true,
            isOtpSent = false,
            authError = null
        )
    }

    fun hideLoginDialog() {
        _dialogState.value = _dialogState.value.copy(
            isLoginDialogVisible = false,
            isOtpSent = false,
            authError = null
        )
    }

    fun onPhoneChange(phone: String) {
        _dialogState.value = _dialogState.value.copy(phoneNumberInput = phone)
    }

    fun onNameChange(name: String) {
        _dialogState.value = _dialogState.value.copy(nameInput = name)
    }

    fun onOtpChange(otp: String) {
        _dialogState.value = _dialogState.value.copy(otpInput = otp)
    }

    fun requestOtp() {
        val current = _dialogState.value
        if (current.phoneNumberInput.length < 8) {
            _dialogState.value = current.copy(authError = "يرجى إدخال رقم هاتف صحيح.")
            return
        }
        _dialogState.value = current.copy(
            isOtpSent = true,
            isAuthLoading = false,
            authError = null
        )
    }

    fun verifyOtpAndLogin() {
        val current = _dialogState.value
        if (current.otpInput.length < 4) {
            _dialogState.value = current.copy(authError = "رمز التحقق يجب أن يتكون من 4 أرقام على الأقل.")
            return
        }
        volunteerRepository.loginWithPhone(current.phoneNumberInput, current.nameInput)
        _dialogState.value = current.copy(
            isLoginDialogVisible = false,
            isAuthLoading = false,
            authError = null
        )
    }

    fun logout() {
        volunteerRepository.logout()
    }

    companion object {
        fun provideFactory(
            volunteerRepo: VolunteerRepository,
            helpCallRepo: HelpCallRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(volunteerRepo, helpCallRepo) as T
            }
        }
    }
}
