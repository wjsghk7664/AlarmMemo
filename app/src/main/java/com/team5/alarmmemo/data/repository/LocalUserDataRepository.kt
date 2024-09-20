package com.team5.alarmmemo.data.repository

import com.team5.alarmmemo.data.model.User

interface LocalUserDataRepository {
    fun saveUserData(user: User)
    fun getUserData():User
}