package com.team5.alarmmemo.data.source.local

import android.content.SharedPreferences
import android.text.SpannableStringBuilder
import com.google.gson.Gson
import com.team5.alarmmemo.data.model.AlarmSetting
import com.team5.alarmmemo.presentation.memo.CheckItem
import javax.inject.Inject

class LocalMemoDataSource @Inject constructor(
    @Memo private val memoSharedPreferences: SharedPreferences,
    @Draw private val drawSharedPreferences: SharedPreferences,
    @Title private val titleSharedPreferences: SharedPreferences,
    @AlarmSettings private val alarmSettingSharedPreferences:SharedPreferences,
    private val gson:Gson
) {
    fun getList():ArrayList<Triple<String,String,SpannableStringBuilder>>{
        val result = ArrayList<Triple<String,String,SpannableStringBuilder>>()

        val all = titleSharedPreferences.all.keys
        for(i in all){
            val memo =memoSharedPreferences.getString(i, null)
                ?.let { gson.fromJson(it, SpannableStringBuilder::class.java) }
                ?: SpannableStringBuilder("")
            result += Triple(i, titleSharedPreferences.getString(i, null)?:"", memo)
        }
        return result

    }

    fun saveAlarmSetting(alarmSetting: AlarmSetting, uniqueId:String){
        val alarmSettingJson = gson.toJson(alarmSetting)

        alarmSettingSharedPreferences.edit().putString(uniqueId,alarmSettingJson).apply()
    }

    fun getAlarmSetting(uniqueId: String):AlarmSetting{
        val json = alarmSettingSharedPreferences.getString(uniqueId,null)
        return json?.let {
            gson.fromJson(it,AlarmSetting::class.java)
        }?:AlarmSetting()
    }

    fun saveMemo(str: SpannableStringBuilder?, uniqueId:String){
        val strJson = gson.toJson(str)
        memoSharedPreferences.edit().putString(uniqueId,strJson).apply()
    }

    fun getMemo(uniqueId: String):SpannableStringBuilder{
        val strJson = memoSharedPreferences.getString(uniqueId,null)


        val str = strJson?.let { gson.fromJson(it,SpannableStringBuilder::class.java) }?: SpannableStringBuilder("")
        return str
    }

    fun saveDraw(drawList: List<CheckItem>, uniqueId:String){
        val drawJson = gson.toJson(drawList)
        drawSharedPreferences.edit().putString(uniqueId,drawJson).apply()
    }

    fun getDraw(uniqueId: String):List<CheckItem>{
        val drawJson = drawSharedPreferences.getString(uniqueId,null)
        val draw = drawJson?.let { gson.fromJson(it,Array<CheckItem>::class.java).toList() }?: listOf()
        return draw
    }

    fun saveTitle(title:String, uniqueId:String){
        titleSharedPreferences.edit().putString(uniqueId,title)
    }

    fun getTitle(uniqueId: String):String{
        return titleSharedPreferences.getString(uniqueId,null)?:""
    }
}