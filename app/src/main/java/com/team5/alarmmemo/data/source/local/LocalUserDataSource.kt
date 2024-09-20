package com.team5.alarmmemo.data.source.local

import android.content.SharedPreferences
import com.team5.alarmmemo.data.model.User
import javax.inject.Inject

class LocalUserDataSource @Inject constructor(@UserPref private val sharedpreferences:SharedPreferences) {

    fun saveUserData(user: User){
        sharedpreferences.edit().putString("email",user.email)
            .putString("name",user.name)
            .putString("password",user.password)
            .apply()
    }

    fun getUserEmail():String{
        return sharedpreferences.getString("email",null)?:"default"
    }

    fun getUserData():User{
        return User(
            sharedpreferences.getString("email",null)?:"",
            sharedpreferences.getString("password",null)?:"",
            sharedpreferences.getString("name",null)?:""
        )
    }
}