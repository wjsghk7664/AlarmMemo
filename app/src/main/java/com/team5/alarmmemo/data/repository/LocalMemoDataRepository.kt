package com.team5.alarmmemo.data.repository

import android.text.SpannableStringBuilder
import com.team5.alarmmemo.data.model.AlarmSetting
import com.team5.alarmmemo.presentation.memo.CheckItem

interface LocalMemoDataRepository {
    fun getList():ArrayList<Triple<String,String,SpannableStringBuilder>>
    fun saveAlarmSetting(alarmSetting: AlarmSetting, uniqueId:String)
    fun getAlarmSetting(uniqueId: String):AlarmSetting
    fun saveMemo(str: SpannableStringBuilder?, uniqueId:String)
    fun getMemo(uniqueId: String):SpannableStringBuilder
    fun saveDraw(drawList: List<CheckItem>, uniqueId:String)
    fun getDraw(uniqueId: String):List<CheckItem>
    fun saveTitle(title:String, uniqueId:String)
    fun getTitle(uniqueId: String):String
}