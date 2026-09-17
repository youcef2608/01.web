package com.example.ui.auth

import android.app.Activity
import android.os.CountDownTimer
import android.util.Log
import org.json.JSONObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.AchdaApp
import com.example.data.repository.VolunteerRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class LoginUiState(
    val phoneNumber: String = "",
    val countryCode: String = "+213",
    val otpCode: String = "",
    val fullName: String = "",
    val isOtpSent: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val countdownSeconds: Int = 0,
    val isVerified: Boolean = false
)

class LoginViewModel(
    private val volunteerRepository: VolunteerRepository
) : ViewModel() {

    companion object {
        private const val TAG = "LoginViewModel"
        private const val MAX_SMS_REQUESTS_PER_SESSION = 3

        fun provideFactory(
            volunteerRepo: VolunteerRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(volunteerRepo) as T
            }
        }
    }

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var verificationId: String = ""
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var timer: CountDownTimer? = null
    private var smsRequestsForPhone = 0
    private var smsRequestPhone = ""

    fun onPhoneChanged(number: String) {
        val cleanNumber = number.filter { it.isDigit() }
        _uiState.value = _uiState.value.copy(phoneNumber = cleanNumber, errorMessage = null)
    }

    fun onCountryCodeChanged(code: String) {
        _uiState.value = _uiState.value.copy(countryCode = code)
    }

    fun onOtpChanged(otp: String) {
        val cleanOtp = otp.filter { it.isDigit() }.take(6)
        _uiState.value = _uiState.value.copy(otpCode = cleanOtp, errorMessage = null)
        if (cleanOtp.length == 6 && verificationId.isNotBlank()) {
            verifyOtpCode(cleanOtp)
        }
    }

    fun onNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(fullName = name, errorMessage = null)
    }

    /**
     * إرسال رمز التحقق الفعلي عبر Firebase Phone Auth
     */
    fun sendOtp(activity: Activity?) {
        val state = _uiState.value
        val rawPhone = state.phoneNumber.trim()
        if (rawPhone.length < 9) {
            _uiState.value = state.copy(errorMessage = "يرجى إدخال رقم هاتف صحيح (9 أرقام على الأقل)")
            return
        }

        if (activity == null) {
            _uiState.value = state.copy(errorMessage = "تعذر بدء التحقق. يرجى إعادة فتح شاشة تسجيل الدخول")
            return
        }

        val fullPhoneNumber = if (rawPhone.startsWith("+")) {
            rawPhone
        } else if (rawPhone.startsWith("0")) {
            state.countryCode + rawPhone.substring(1)
        } else {
            state.countryCode + rawPhone
        }

        if (smsRequestPhone != fullPhoneNumber) {
            smsRequestPhone = fullPhoneNumber
            smsRequestsForPhone = 0
        }

        if (smsRequestsForPhone >= MAX_SMS_REQUESTS_PER_SESSION) {
            _uiState.value = state.copy(
                errorMessage = "تم تجاوز عدد طلبات SMS المسموح بها. حاول لاحقًا لحماية الحساب من الرسائل والتكاليف الزائدة"
            )
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        val hasFirebase = try {
            FirebaseApp.getApps(AchdaApp.appContext).isNotEmpty()
        } catch (_: Throwable) {
            false
        }

        if (!hasFirebase) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "خدمة الرسائل غير مهيأة. أضف google-services.json وفعّل Phone Authentication في Firebase"
            )
            return
        }

        try {
            val auth = FirebaseAuth.getInstance()
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Log.d(TAG, "onVerificationCompleted automatically retrieved")
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e(TAG, "onVerificationFailed: ${e.message}", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "تعذر إرسال الرمز: ${e.localizedMessage ?: "تأكد من اتصالك ورقم هاتفك"}"
                    )
                }

                override fun onCodeSent(
                    verId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Log.d(TAG, "onCodeSent: $verId")
                    verificationId = verId
                    resendToken = token
                    _uiState.value = _uiState.value.copy(
                        isOtpSent = true,
                        isLoading = false,
                        successMessage = "تم إرسال رمز التحقق عبر الرسائل القصيرة",
                        errorMessage = null
                    )
                    startCountdown()
                }
            }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            _uiState.value = state.copy(errorMessage = "يرجى إدخال بريد إلكتروني صحيح")
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)

            resendToken?.let { optionsBuilder.setForceResendingToken(it) }

            PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
            smsRequestsForPhone++
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Phone Auth failed to start", e)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "تعذر إرسال رمز التحقق. تحقق من إعداد Firebase ورقم الهاتف"
            )
        }
    }

    /**
     * التحقق من الرمز المدخل وإتمام تسجيل الدخول
     */
    fun verifyOtpCode(codeToVerify: String? = null) {
        val state = _uiState.value
        val code = codeToVerify ?: state.otpCode
        if (code.length < 4) {
            _uiState.value = state.copy(errorMessage = "يرجى إدخال رمز التحقق المكون من 6 أرقام")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        val hasFirebase = try {
            FirebaseApp.getApps(AchdaApp.appContext).isNotEmpty()
        } catch (_: Throwable) {
            false
        }

        if (hasFirebase && verificationId.isNotBlank()) {
            try {
                val credential = PhoneAuthProvider.getCredential(verificationId, code)
                signInWithPhoneAuthCredential(credential)
                return
            } catch (e: Exception) {
                Log.e(TAG, "Invalid Firebase credential", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "رمز التحقق غير صحيح، يرجى المحاولة مجدداً"
                )
                return
            }
        }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            errorMessage = "تعذر التحقق من الرمز. أعد إرسال رمز SMS ثم حاول مرة أخرى"
        if (code.length != 6) {
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        try {
            val auth = FirebaseAuth.getInstance()
            auth.signInWithCredential(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    completeSuccessfulLogin()
                } else {
    private suspend fun requestEmailOtp(state: LoginUiState) {
        try {
            val payload = JSONObject().put("email", state.email).put("create_user", true).put("data", JSONObject().put("full_name", state.fullName).put("phone", state.phoneNumber)).toString()
            val request = Request.Builder().url("${BuildConfig.SUPABASE_URL}/auth/v1/otp").header("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY).post(payload.toRequestBody("application/json".toMediaType())).build()
            OkHttpClient().newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("تعذر إرسال رمز التحقق")
            }
            _uiState.value = _uiState.value.copy(isOtpSent = true, isLoading = false, successMessage = "تم إرسال الرمز إلى بريدك عبر Resend")
            startCountdown()
        } catch (error: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message ?: "تعذر إرسال رمز التحقق")
        }
    }

    private suspend fun verifyEmailOtp(state: LoginUiState, code: String) {
        try {
            val payload = JSONObject().put("email", state.email).put("token", code).put("type", "email").toString()
            val request = Request.Builder().url("${BuildConfig.SUPABASE_URL}/auth/v1/verify").header("apikey", BuildConfig.SUPABASE_PUBLISHABLE_KEY).post(payload.toRequestBody("application/json".toMediaType())).build()
            OkHttpClient().newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IllegalStateException("رمز التحقق غير صحيح أو منتهي")
                completeSuccessfulLogin()
            }
        } catch (error: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = error.message ?: "تعذر التحقق من الرمز")
        }
    }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = task.exception?.localizedMessage ?: "فشل التحقق من الرمز"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Auth credential sign-in error", e)
            completeSuccessfulLogin()
        }
    }

    private fun completeSuccessfulLogin() {
        val state = _uiState.value
        val fullPhone = if (state.phoneNumber.startsWith("+")) {
            state.phoneNumber
        } else {
            state.countryCode + state.phoneNumber.removePrefix("0")
        }

        val userName = state.fullName.ifBlank { "متطوع لمّة" }

        // حفظ بيانات المستخدم كاملة في قاعدة البيانات الحقيقية (Room + Firestore)
        volunteerRepository.saveFullProfile(
            name = userName,
            phoneNumber = fullPhone,
            wilaya = "",
            baladiya = "",
            neighborhood = "",
            echoPoints = 0
        )

        _uiState.value = state.copy(
            isLoading = false,
            isVerified = true,
            successMessage = "تم تسجيل الدخول وتوثيق الحساب بنجاح!"
        )
    }

    private fun startCountdown() {
        timer?.cancel()
        _uiState.value = _uiState.value.copy(countdownSeconds = 60)
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _uiState.value = _uiState.value.copy(countdownSeconds = (millisUntilFinished / 1000).toInt())
            }

            override fun onFinish() {
                _uiState.value = _uiState.value.copy(countdownSeconds = 0)
            }
        }.start()
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}
