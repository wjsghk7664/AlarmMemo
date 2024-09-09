package com.team5.alarmmemo.data.repository.lastmodify

import android.content.SharedPreferences
import com.team5.alarmmemo.data.source.local.LastModify
import javax.inject.Inject

class LocalLastModifyRepositoryImpl @Inject constructor(@LastModify private val sharedPreferences: SharedPreferences):LastModifyRepository {
    override fun getLastModifyTime(uniqueId: String, callback: (Long) -> Unit) {
        val time = sharedPreferences.getLong(uniqueId,0L)
        callback(time)
    }

    override fun saveLastModifyTime(time: Long, uniqueId: String) {
        sharedPreferences.edit().putLong(uniqueId,time).apply()
    }
}