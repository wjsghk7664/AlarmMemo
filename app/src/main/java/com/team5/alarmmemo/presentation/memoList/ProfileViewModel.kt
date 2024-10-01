package com.team5.alarmmemo.presentation.memoList

import androidx.lifecycle.ViewModel
import com.team5.alarmmemo.UiState
import com.team5.alarmmemo.data.model.User
import com.team5.alarmmemo.data.repository.RemoteUserDataRepository
import com.team5.alarmmemo.data.repository.memo.MemoDataRepository
import com.team5.alarmmemo.data.repository.memo.RemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val remoteUserDataRepository: RemoteUserDataRepository,
    @RemoteRepository private val remoteMemoDataRepository: MemoDataRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<String?>>(UiState.Init)
    val uiState = _uiState.asStateFlow()



    fun modifyUserData(user: User) {
        remoteUserDataRepository.addOrModifyUserData(user) { e ->
            if (e == null) {
                _uiState.value = UiState.Success(user.name)
            } else {
                _uiState.value = UiState.Failure("아이디 변경에 실패하였습니다.")
            }
        }
    }

    fun deleteId(id: String) {
        remoteMemoDataRepository.removeIdContent {
            if (it) {
                remoteUserDataRepository.deleteUserData(id) { e ->
                    if (e == null) {
                        _uiState.value = UiState.Success(null)
                    } else {
                        _uiState.value = UiState.Failure("회원 탈퇴 실패")
                    }
                }
            } else {
                _uiState.value = UiState.Failure("회원 탈퇴 실패")
            }
        }

    }
}