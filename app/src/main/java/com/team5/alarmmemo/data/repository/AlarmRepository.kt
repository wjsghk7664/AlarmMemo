package com.team5.alarmmemo.data.repository

interface AlarmRepository {
    fun getAlarms(): HashSet<Triple<Long,String,Boolean>>
    fun setAlarms(alarms: HashSet<Triple<Long,String,Boolean>>)
}