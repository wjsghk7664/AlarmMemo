package com.team5.alarmmemo.data.repository.lastmodify

interface LastModifyRepository {
    fun getLastModifyTime(uniqueId:String, callback:(Long)->Unit)
    fun saveLastModifyTime(time:Long, uniqueId: String)
}