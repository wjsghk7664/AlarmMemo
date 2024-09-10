package com.team5.alarmmemo.data.repository

import com.team5.alarmmemo.data.model.User

interface RemoteUserDataRepository {
    fun checkEmailDuplicate(email: String, callback: (Boolean, String?) -> Unit)
    fun addOrModifyUserData(user: User, callback: (String?) -> Unit)
    fun deleteUserData(email: String, callback: (String?) -> Unit)
    fun Login(emailOrToken:String, password:String? = null, callback: (User?, String?) -> Unit)
}