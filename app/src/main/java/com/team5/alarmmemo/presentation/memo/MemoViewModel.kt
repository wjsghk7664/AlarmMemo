package com.team5.alarmmemo.presentation.memo

import android.text.SpannableStringBuilder
import androidx.lifecycle.ViewModel
import com.example.alarmmemo.presentation.memoLogin.UiState
import com.team5.alarmmemo.data.model.AlarmSetting
import com.team5.alarmmemo.data.repository.LocalMemoDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MemoViewModel @Inject constructor(private val localMemoDataRepository: LocalMemoDataRepository): ViewModel() {
    private val _uiState = MutableStateFlow<UiState<HashMap<String,Any>>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun saveAlarmSetting(alarmSetting: AlarmSetting,uniqueId:String,userId:String){
        localMemoDataRepository.saveAlarmSetting(alarmSetting, uniqueId)
    }

    fun saveMemo(str: SpannableStringBuilder?,uniqueId:String,userId: String){
        localMemoDataRepository.saveMemo(str, uniqueId)
    }

    fun saveDraw(draw:List<CheckItem>, uniqueId: String, userId: String){
        localMemoDataRepository.saveDraw(draw,uniqueId)
    }

    fun saveTitle(title:String, uniqueId:String, userId: String){
        localMemoDataRepository.saveTitle(title, uniqueId)
    }

    fun getAlarmSetting(uniqueId: String): AlarmSetting{
        return localMemoDataRepository.getAlarmSetting(uniqueId)
    }

    fun getMemo(uniqueId: String): SpannableStringBuilder{
        return localMemoDataRepository.getMemo(uniqueId)
    }

    fun getDraw(uniqueId: String): List<CheckItem>{
        return localMemoDataRepository.getDraw(uniqueId)
    }

    fun getTitle(uniqueId: String): String{
        return localMemoDataRepository.getTitle(uniqueId)
    }

    fun getMemoDatas(uniqueId: String){
        var datas = HashMap<String,Any>()
        datas.put("title",getTitle(uniqueId))
        datas.put("settings",getAlarmSetting(uniqueId))
        datas.put("memo",getMemo(uniqueId))
        datas.put("draw",getDraw(uniqueId))
        _uiState.value = UiState.Success(datas)
    }
}