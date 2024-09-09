package com.team5.alarmmemo.data.source.local

import android.content.SharedPreferences
import androidx.core.content.edit
import com.team5.alarmmemo.Constants.USER_INPUT_EMAIL
import javax.inject.Inject

class LocalSignUpDataSource @Inject constructor(
    @SignUp private val signUpSharedPreferences: SharedPreferences
){
    fun saveUserInputEmail(email:String?){
        signUpSharedPreferences.edit { putString(USER_INPUT_EMAIL, email) }
    }

    fun getUserInputEmail(): String?{
        return signUpSharedPreferences.getString(USER_INPUT_EMAIL, null)
    }
}