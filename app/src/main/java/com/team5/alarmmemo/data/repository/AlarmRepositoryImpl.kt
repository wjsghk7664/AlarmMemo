package com.team5.alarmmemo.data.repository

import android.content.SharedPreferences
import com.team5.alarmmemo.data.source.local.ActiveAlarms
import javax.inject.Inject

class AlarmRepositoryImpl @Inject constructor(@ActiveAlarms private val activeSharedPreferences: SharedPreferences): AlarmRepository {
    override fun getAlarms(): HashSet<Triple<Long,String,Boolean>> {
        val map = activeSharedPreferences.all
        val list = HashSet<Triple<Long,String,Boolean>>()
        map.forEach { data, id ->
            val datas = (data as String).split("_")
            list+=Triple(datas[0].toLong(),id as String,datas[1].toBoolean())
        }
        return list
    }

    override fun setAlarms(alarms: HashSet<Triple<Long,String,Boolean>>) {
        val edit = activeSharedPreferences.edit()
        alarms.forEach {
            edit.putString("${it.first}_${it.third}",it.second)
        }
        edit.apply()
    }
}