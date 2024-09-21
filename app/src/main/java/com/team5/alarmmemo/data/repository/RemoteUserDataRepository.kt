package com.team5.alarmmemo.data.repository

import com.team5.alarmmemo.data.model.User

interface RemoteUserDataRepository {
    fun findUser(email: String, callback: (Boolean, String?, String?) -> Unit)
    fun resetPassword(email:String, newPassword: String, callback: (String?) -> Unit)
    fun addOrModifyUserData(user: User, callback: (String?) -> Unit)
    fun deleteUserData(email: String, callback: (String?) -> Unit)
    fun login(emailOrToken:String, password:String? = null, callback: (User?, String?) -> Unit)
}