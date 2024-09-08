package com.team5.alarmmemo.data.repository.memo

import android.text.SpannableStringBuilder
import com.team5.alarmmemo.data.model.AlarmSetting
import com.team5.alarmmemo.presentation.memo.CheckItem

interface MemoDataRepository {
    fun getList(callback:(ArrayList<Triple<String,String,SpannableStringBuilder>>)->Unit)
    fun saveAlarmSetting(alarmSetting: AlarmSetting, uniqueId:String)
    fun getAlarmSetting(uniqueId: String, callback:(AlarmSetting)->Unit)
    fun saveMemo(str: SpannableStringBuilder?, uniqueId:String)
    fun getMemo(uniqueId: String, callback:(SpannableStringBuilder)->Unit)
    fun saveDraw(drawList: List<CheckItem>, uniqueId:String)
    fun getDraw(uniqueId: String, callback:(List<CheckItem>)->Unit)
    fun saveTitle(title:String, uniqueId:String)
    fun getTitle(uniqueId: String,callback: (String) -> Unit)
    fun getAllAlarms(callback:(List<AlarmSetting>,List<String>)->Unit)
}