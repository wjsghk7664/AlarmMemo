package com.team5.alarmmemo.data.repository

import com.team5.alarmmemo.data.model.User

interface RemoteUserDataRepository {
    fun getByUserEmail(email:String ,callback:(Boolean,String?)->Unit)
    fun addOrModifyUserData(user: User, callback: (Boolean, String?) -> Unit)
    fun deleteUserData(email: String, callback: (Boolean, String?) -> Unit)
    fun Login(emailOrToken:String, password:String? = null, callback: (User?, String?) -> Unit)
}