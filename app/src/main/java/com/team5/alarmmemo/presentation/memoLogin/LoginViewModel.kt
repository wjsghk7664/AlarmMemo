package com.team5.alarmmemo.presentation.memoLogin

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.team5.alarmmemo.UiState
import com.team5.alarmmemo.data.model.User
import com.team5.alarmmemo.data.repository.RemoteUserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: RemoteUserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<User>>(UiState.Init)
    val uiState = _uiState.asStateFlow()


    // 로그인 처리
    fun login(emailOrToken: String, name:String = "", password: String?=null) {
        _uiState.value = UiState.Loading
        repository.Login(emailOrToken, password) { user, message ->
            if(user!=null){
                _uiState.value = UiState.Success(user)
            }else{
                if(message=="Socail Login Init"){
                    val newUser = User(email =emailOrToken,name=name)
                    repository.addOrModifyUserData(newUser){ bool, e ->
                        if(bool){
                            _uiState.value = UiState.Success(newUser)
                        }else{
                            _uiState.value = UiState.Failure(e?:"")
                        }

                    }
                }else{
                    _uiState.value = UiState.Failure("fail to login")
                }
            }
        }
    }
}