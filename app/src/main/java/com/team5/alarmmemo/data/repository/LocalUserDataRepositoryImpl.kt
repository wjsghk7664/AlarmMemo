package com.team5.alarmmemo.data.repository

import com.team5.alarmmemo.data.model.User
import com.team5.alarmmemo.data.source.local.LocalUserDataSource
import javax.inject.Inject

class LocalUserDataRepositoryImpl @Inject constructor(private val localUserDataSource: LocalUserDataSource):LocalUserDataRepository {
    override fun saveUserData(user: User) {
        localUserDataSource.saveUserData(user)
    }

    override fun getUserData(): User {
        return localUserDataSource.getUserData()
    }
}