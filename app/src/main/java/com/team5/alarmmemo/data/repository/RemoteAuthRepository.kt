package com.team5.alarmmemo.data.repository

import com.google.firebase.auth.FirebaseUser

interface RemoteAuthRepository {
    fun saveUserInputEmail(email: String?)
    fun getUserInputEmail(): String?
    fun createTempAccount(email: String, callback: (FirebaseUser?, String?) -> Unit)
    fun sendEmailVerification(user: FirebaseUser?, callback: (String?) -> Unit)
    fun checkEmailVerification(callback: (Boolean, String?) -> Unit)
    fun createAccount(email: String, password: String, callback: (String?) -> Unit)
    fun loginTempAccount(email: String, callback: (String?) -> Unit)
    fun deleteCurrentUserAccount(callback: (String?) -> Unit)
}