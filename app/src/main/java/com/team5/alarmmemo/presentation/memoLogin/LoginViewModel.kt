package com.team5.alarmmemo.presentation.memoLogin

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.team5.alarmmemo.UiState
import com.team5.alarmmemo.data.model.User
import com.team5.alarmmemo.data.repository.LocalUserDataRepository
import com.team5.alarmmemo.data.repository.PrivacyAgreementSettingRepository
import com.team5.alarmmemo.data.repository.RemoteUserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities.Private

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: RemoteUserDataRepository,
    private val userRepository:LocalUserDataRepository,
    private val privacyAgreementSettingRepository: PrivacyAgreementSettingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<User>>(UiState.Init)
    val uiState = _uiState.asStateFlow()


    // 로그인 처리
    fun login(emailOrToken: String, name: String = "", password: String? = null) {
        _uiState.value = UiState.Loading
        repository.Login(emailOrToken, password) { user, message ->
            if (user != null) {
                userRepository.saveUserData(user)
                _uiState.value = UiState.Success(user)
            } else {
                if (message == "Social Login Init") {
                    val newUser = User(email = emailOrToken, name = name)
                    repository.addOrModifyUserData(newUser) { e ->
                        if (e == null) {
                            userRepository.saveUserData(newUser)
                            _uiState.value = UiState.Success(newUser)
                        } else {
                            _uiState.value = UiState.Failure(e ?: "")
                        }

                    }
                } else {
                    _uiState.value = UiState.Failure(message?:"fail to login")
                }
            }
        }
    }

    fun autoLogin(){
        val user = userRepository.getUserData()
        if(user==User()) return

        val agreement = getAgreement()
        if(!agreement) return

        login(user.email,user.name,user.password)
    }

    fun getAgreement():Boolean{
        return privacyAgreementSettingRepository.getPrivacy()
    }
    fun setAgreement(bool:Boolean){
        privacyAgreementSettingRepository.setPrivacy(bool)
    }
}