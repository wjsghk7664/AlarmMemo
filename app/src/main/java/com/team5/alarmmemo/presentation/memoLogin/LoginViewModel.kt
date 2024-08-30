package com.example.alarmmemo.presentation.memoLogin

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.auth.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor( ):ViewModel(){
    private val _uiState = MutableStateFlow<UiState<User>>(UiState.Init)
    val uiState = _uiState.asStateFlow()

    fun loginCheck(id:String, password:String, isCache:Boolean){
//        checkLoginUseCase(id,password){ user ->
//            if(user!=null){
//                var result=true
//                if(isCache){
//                    result = cacheLoginDataUseCase(id,password)?:false
//                }
//                if(result){
//                    _uiState.value = UiState.Success(user)
//                }else{
//                    _uiState.value = UiState.Failure("cachefail")
//                }
//            }else{
//                _uiState.value = UiState.Failure("fail")
//            }
//        }
    }

//    fun autoLogin(){
//        val result = getCacheLoginDataUseCase()
//        if(result!=null){
//            result.let {
//                val id = it.first
//                val password = it.second
//                if(id!=null&&password!=null){
//                    loginCheck(id,password,true)
//                }else{
//                    _uiState.value = UiState.Failure("failAutoLogin")
//                }
//            }
//        }else{
//            _uiState.value = UiState.Failure("failAutoLogin")
//        }
//
//
//    }
}