package com.team5.alarmmemo.data.repository.memo

import android.text.SpannableStringBuilder
import com.team5.alarmmemo.data.model.AlarmSetting
import com.team5.alarmmemo.data.source.local.LocalMemoDataSource
import com.team5.alarmmemo.presentation.memo.CheckItem
import javax.inject.Inject

class LocalMemoDataRepositoryImpl @Inject constructor(private val localMemoDataSource: LocalMemoDataSource):
    MemoDataRepository {
    override fun getList(callback:(ArrayList<Triple<String,String,SpannableStringBuilder>>)->Unit) {
        callback(localMemoDataSource.getList())
    }

    override fun saveAlarmSetting(alarmSetting: AlarmSetting, uniqueId: String) {
        localMemoDataSource.saveAlarmSetting(alarmSetting,uniqueId)
    }

    override fun getAlarmSetting(uniqueId: String, callback:(AlarmSetting)->Unit) {
        callback(localMemoDataSource.getAlarmSetting(uniqueId))
    }

    override fun saveMemo(str: SpannableStringBuilder?, uniqueId: String) {
        localMemoDataSource.saveMemo(str,uniqueId)
    }

    override fun getMemo(uniqueId: String, callback:(SpannableStringBuilder)->Unit) {
        callback(localMemoDataSource.getMemo(uniqueId))
    }

    override fun saveDraw(drawList: List<CheckItem>, uniqueId: String) {
        localMemoDataSource.saveDraw(drawList,uniqueId)
    }

    override fun getDraw(uniqueId: String, callback:(List<CheckItem>)->Unit) {
        callback(localMemoDataSource.getDraw(uniqueId))
    }

    override fun saveTitle(title: String, uniqueId: String) {
        localMemoDataSource.saveTitle(title,uniqueId)
    }

    override fun getTitle(uniqueId: String, callback: (String) -> Unit) {
        callback(localMemoDataSource.getTitle(uniqueId))
    }

    override fun getAllAlarms(callback: (List<AlarmSetting>,List<String>) -> Unit) {
        val results = localMemoDataSource.getAllAlarms()
        callback(results.first,results.second)
    }
}