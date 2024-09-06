package com.team5.alarmmemo.data.repository

import android.text.SpannableStringBuilder
import com.team5.alarmmemo.data.model.AlarmSetting
import com.team5.alarmmemo.data.source.local.LocalMemoDataSource
import com.team5.alarmmemo.presentation.memo.CheckItem
import javax.inject.Inject

class LocalMemoDataRepositoryImpl @Inject constructor(private val localMemoDataSource: LocalMemoDataSource):LocalMemoDataRepository {
    override fun getList(): ArrayList<Triple<String, String, SpannableStringBuilder>> {
        return localMemoDataSource.getList()
    }

    override fun saveAlarmSetting(alarmSetting: AlarmSetting, uniqueId: String) {
        localMemoDataSource.saveAlarmSetting(alarmSetting,uniqueId)
    }

    override fun getAlarmSetting(uniqueId: String): AlarmSetting {
        return localMemoDataSource.getAlarmSetting(uniqueId)
    }

    override fun saveMemo(str: SpannableStringBuilder?, uniqueId: String) {
        localMemoDataSource.saveMemo(str,uniqueId)
    }

    override fun getMemo(uniqueId: String): SpannableStringBuilder {
        return localMemoDataSource.getMemo(uniqueId)
    }

    override fun saveDraw(drawList: List<CheckItem>, uniqueId: String) {
        localMemoDataSource.saveDraw(drawList,uniqueId)
    }

    override fun getDraw(uniqueId: String): List<CheckItem> {
        return localMemoDataSource.getDraw(uniqueId)
    }

    override fun saveTitle(title: String, uniqueId: String) {
        localMemoDataSource.saveTitle(title,uniqueId)
    }

    override fun getTitle(uniqueId: String): String {
        return localMemoDataSource.getTitle(uniqueId)
    }
}