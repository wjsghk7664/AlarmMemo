package com.team5.alarmmemo.presentation.memoLogin

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.auth.User
import com.team5.alarmmemo.data.repository.RemoteUserDataRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: RemoteUserDataRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<User>>(UiState.Init)
    val uiState = _uiState.asStateFlow()

    private val _actionState = MutableStateFlow<UiState<Unit>>(UiState.Init)
    val actionState = _actionState.asStateFlow()

    private val _emailCheckState = MutableStateFlow<UiState<String?>>(UiState.Init)
    val emailCheckState = _emailCheckState.asStateFlow()

    // 이메일로 사용자 확인
    fun checkEmail(email: String) {
        _emailCheckState.value = UiState.Loading
        repository.getByUserEmail(email) { isSuccess, message ->
            if (isSuccess) {
                _emailCheckState.value = UiState.Success(message)
            } else {
                _emailCheckState.value = UiState.Failure(message ?: "Unknown error")
            }
        }
    }

    // 사용자 추가 또는 수정
//    fun addOrModifyUser(user: User) {
//        _actionState.value = UiState.Loading
//        repository.addOrModifyUserData(user) { isSuccess, message ->
//            if (isSuccess) {
//                _actionState.value = UiState.Success(Unit)
//            } else {
//                _actionState.value = UiState.Failure(message ?: "Failed to add or modify user")
//            }
//        }
//    }

    // 사용자 삭제
    fun deleteUser(email: String) {
        _actionState.value = UiState.Loading
        repository.deleteUserData(email) { isSuccess, message ->
            if (isSuccess) {
                _actionState.value = UiState.Success(Unit)
            } else {
                _actionState.value = UiState.Failure(message ?: "Failed to delete user")
            }
        }
    }

    // 로그인 처리
    fun login(emailOrToken: String, password: String?) {
        _uiState.value = UiState.Loading
        repository.Login(emailOrToken, password) { user, message ->
            if (user != null) {
//                _uiState.value = UiState.Success(user)
            } else {
                _uiState.value = UiState.Failure(message ?: "Login failed")
            }
        }
    }
}