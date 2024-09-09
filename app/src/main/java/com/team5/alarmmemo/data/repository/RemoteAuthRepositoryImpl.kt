package com.team5.alarmmemo.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.team5.alarmmemo.Constants.TEMP_PASSWORD
import com.team5.alarmmemo.data.source.local.LocalSignUpDataSource
import javax.inject.Inject

class RemoteAuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val localSignUpSource: LocalSignUpDataSource
) :
    RemoteAuthRepository {

    // 이메일 인증 시 유저가 작성한 이메일 저장
    override fun saveUserInputEmail(email: String?) {
        localSignUpSource.saveUserInputEmail(email)
    }

    // 이메일 인증 시 유저가 작성한 이메일 불러오기
    override fun getUserInputEmail() :String?{
        return localSignUpSource.getUserInputEmail()
    }

    // 이메일 인증을 위한 임시 계정 생성
    override fun createTempAccount(email: String, callback: (FirebaseUser?, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, TEMP_PASSWORD)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(task.result.user, null)
                } else {
                    callback(null, task.exception?.message)
                }
            }
    }

    // 이메일 인증 요청 수신
    override fun sendEmailVerification(user: FirebaseUser?, callback: (String?) -> Unit) {
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(null)
            } else {
                callback(task.exception?.message)
            }
        }
    }

    // 이메일 인증 확인
    override fun checkEmailVerification(callback: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(user.isEmailVerified, null)
            } else {
                callback(false, task.exception?.message)
            }
        }
    }

    // 계정 생성
    override fun createAccount(
        email: String,
        password: String,
        callback: (String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(null)
                } else {
                    callback(task.exception?.message)
                }
            }
    }

    // 임시 계정으로 로그인
    override fun loginTempAccount(email: String, callback: (String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, TEMP_PASSWORD)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(null)
                } else {
                    callback(task.exception?.message)
                }
            }
    }

    // 로그인한 유저 삭제
    override fun deleteCurrentUserAccount(callback: (String?) -> Unit) {
        auth.currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                callback(null)
            } else {
                callback(task.exception?.message)
            }
        }
    }
}