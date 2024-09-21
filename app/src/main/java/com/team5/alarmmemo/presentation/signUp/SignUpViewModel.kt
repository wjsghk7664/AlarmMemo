package com.team5.alarmmemo.presentation.signUp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team5.alarmmemo.data.model.User
import com.team5.alarmmemo.R
import com.team5.alarmmemo.data.repository.RemoteAuthRepository
import com.team5.alarmmemo.data.repository.RemoteUserDataRepository
import com.team5.alarmmemo.UiState
import com.team5.alarmmemo.util.AccountUtil.formatTime
import com.team5.alarmmemo.util.AccountUtil.hashPassword
import com.team5.alarmmemo.util.AccountUtil.isValidEmail
import com.team5.alarmmemo.util.AccountUtil.isValidPassword
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: RemoteAuthRepository,
    private val userDataRepository: RemoteUserDataRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<SignUpEvent?>>(UiState.Init)
    val uiState: StateFlow<UiState<SignUpEvent?>> = _uiState.asStateFlow()

    private val _emailValidation = MutableSharedFlow<String>(1)
    val emailValidation: SharedFlow<String> = _emailValidation.asSharedFlow()

    private val _nameValidation = MutableSharedFlow<String>(1)
    val nameValidation: SharedFlow<String> = _nameValidation.asSharedFlow()

    private val _passwordValidation = MutableSharedFlow<String>(1)
    val passwordValidation: SharedFlow<String> = _passwordValidation.asSharedFlow()

    private val _passwordCheckValidation = MutableSharedFlow<String>(1)
    val passwordCheckValidation: SharedFlow<String> = _passwordCheckValidation.asSharedFlow()

    private var isEmailVerified = false
    private val maxTime = 180

    private var isNameValid = false
    private var isPasswordValid = false
    private var isPasswordCheckValid = false

    fun emailVerification(email: String) {
        viewModelScope.launch {
            if (email.isNotBlank()) {
                if (isValidEmail(email)) {
                    _uiState.value = UiState.Loading

                    userDataRepository.findUser(email) { isFind, _, error ->
                        if (error == null) {
                            if (!isFind) {
                                emitValidation(_emailValidation, getString(R.string.email_validation_loading))
                                sendEmailVerification(email)
                            } else {
                                _uiState.value = UiState.Init
                                emitValidation(_emailValidation, getString(R.string.email_validation_duplicated))
                            }
                        } else {
                            _uiState.value = UiState.Failure("Store | 이메일 중복 찾기 에러: $error")
                        }
                    }
                } else {
                    _emailValidation.emit(getString(R.string.email_invalid_format))
                }
            } else {
                _emailValidation.emit(getString(R.string.email_empty))
            }
        }
    }

    private fun sendEmailVerification(email: String){
        authRepository.createTempAccount(email) { user, error ->
            if (error == null) {
                authRepository.saveUserInputEmail(email)
                authRepository.sendEmailVerification(user) { error ->
                    if (error == null) {
                        checkEmailVerification()
                    } else {
                        _uiState.value =
                            UiState.Failure("Auth | 임시 계정 생성 에러: $error")
                    }
                }
            } else {
                _uiState.value =
                    UiState.Failure("Auth | 이메일 인증 요청 수신 에러: $error")
            }
        }
    }

    private fun checkEmailVerification() {
        viewModelScope.launch {
            val timerJob = launch {
                val startTime = System.currentTimeMillis()
                var timerRun = true

                while (timerRun) {
                    val currentTime = System.currentTimeMillis()
                    val elapsedTime = ((currentTime - startTime) / 1000).toInt()
                    val remainingTime = maxTime - elapsedTime

                    if (remainingTime <= 0) {
                        timerRun = false
                    } else {
                        _emailValidation.emit(getString(R.string.email_validation_period, formatTime(remainingTime)))
                    }

                    delay(1000L)
                }
            }

            val pollingJob = launch {
                var pollingRun = true

                while (pollingRun) {
                    authRepository.checkEmailVerification { complete, error ->
                        if (error == null) {
                            if (complete) {
                                isEmailVerified = complete
                                pollingRun = false
                                timerJob.cancel()
                            }
                        } else {
                            _uiState.value = UiState.Failure("FireStore | 이메일 인증 확인 에러: $error")
                            pollingRun = false
                            timerJob.cancel()
                        }
                    }

                    delay(2000L)
                }
            }

            timerJob.join()

            if (isEmailVerified) {
                _uiState.value = UiState.Success(SignUpEvent.EMAIL_VERIFICATION)
                _emailValidation.emit(getString(R.string.email_validation_compeleted))
            }
        }
    }

    fun signUpSubmitBtnAction(email: String, name:String, password: String, passwordCheck:String){
        viewModelScope.launch {
            if (!isEmailVerified) {
                _emailValidation.emit(getString(R.string.email_validation_request))
            }

            if (isEmailVerified) {
                if (name.isBlank()) {
                    _nameValidation.emit(getString(R.string.name_empty))
                } else {
                    _nameValidation.emit("")
                    isNameValid = true
                }
            }

            if(isEmailVerified && isNameValid){
                if (password.isBlank()) {
                    _passwordValidation.emit(getString(R.string.password_empty))
                } else if (!isValidPassword(password)) {
                    _passwordValidation.emit(getString(R.string.password_invalid_format))
                } else {
                    _passwordValidation.emit("")
                    isPasswordValid = true
                }
            }

            if (isEmailVerified && isNameValid && isPasswordValid) {
                if (password != passwordCheck) {
                    _passwordCheckValidation.emit(getString(R.string.password_check_error))
                } else {
                    _passwordCheckValidation.emit("")
                    isPasswordCheckValid = true
                }
            }

            if (isEmailVerified && isNameValid && isPasswordValid && isPasswordCheckValid) {
                _uiState.value = UiState.Success(SignUpEvent.SIGN_UP)

                val hashPassword = hashPassword(password)
                val user = User(email, hashPassword, name)

                userDataRepository.addOrModifyUserData(user) { error ->
                    if (error != null) {
                        _uiState.value = UiState.Failure("FireStore | 유저 정보 저장 에러: $error")
                    }
                }
            }

        }
    }

    fun deleteTempAccount() {
        val storedEmail = authRepository.getUserInputEmail()
        if (storedEmail != null) {
            authRepository.deleteCurrentUserAccount { error ->
                if (error == null) {
                    authRepository.saveUserInputEmail(null)
                }else{
                    _uiState.value = UiState.Failure("Auth | 임시 계정 삭제 실패: $error")
                }
            }
        }
    }

    private fun emitValidation(validation:MutableSharedFlow<String>, msg:String){
        viewModelScope.launch {
            validation.emit(msg)
        }
    }

    private fun getString(resId: Int, vararg str:String?): String {
        return if(str.isEmpty()) context.getString(resId) else context.getString(resId, *str)
    }
}