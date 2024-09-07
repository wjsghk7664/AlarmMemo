package com.team5.alarmmemo.presentation.memo

import android.text.SpannableStringBuilder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarmmemo.presentation.memoLogin.UiState
import com.team5.alarmmemo.data.model.AlarmSetting
import com.team5.alarmmemo.data.repository.memo.LocalRepository
import com.team5.alarmmemo.data.repository.memo.MemoDataRepository
import com.team5.alarmmemo.data.repository.memo.RemoteMemoDataRepositoryImpl
import com.team5.alarmmemo.data.repository.memo.RemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class MemoViewModel @Inject constructor(@LocalRepository private val localMemoDataRepository: MemoDataRepository, @RemoteRepository private val remoteMemoDataRepository:MemoDataRepository ): ViewModel() {
    private val _uiState = MutableStateFlow<UiState<HashMap<String,Any>>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    fun setInitalSetting(userId: String){
        (remoteMemoDataRepository as RemoteMemoDataRepositoryImpl).setUserid(userId)
    }

    //여기서 알람추가 작업
    fun saveAlarmSetting(alarmSetting: AlarmSetting,uniqueId:String){
        localMemoDataRepository.saveAlarmSetting(alarmSetting, uniqueId)
        remoteMemoDataRepository.saveAlarmSetting(alarmSetting, uniqueId)
    }

    fun saveMemo(str: SpannableStringBuilder?,uniqueId:String){
        localMemoDataRepository.saveMemo(str, uniqueId)
        remoteMemoDataRepository.saveMemo(str, uniqueId)
    }

    fun saveDraw(draw:List<CheckItem>, uniqueId: String){
        localMemoDataRepository.saveDraw(draw,uniqueId)
        remoteMemoDataRepository.saveDraw(draw,uniqueId)
    }

    fun saveTitle(title:String, uniqueId:String){
        localMemoDataRepository.saveTitle(title, uniqueId)
        remoteMemoDataRepository.saveTitle(title, uniqueId)
    }

    suspend fun getAlarmSetting(uniqueId: String,isLocal: Boolean):AlarmSetting = suspendCoroutine{ cont ->
        if(isLocal){
            localMemoDataRepository.getAlarmSetting(uniqueId){
                cont.resume(it)
            }
        }else{
            remoteMemoDataRepository.getAlarmSetting(uniqueId){
                cont.resume(it)
            }
        }

    }

    suspend fun getMemo(uniqueId: String,isLocal: Boolean):SpannableStringBuilder = suspendCoroutine{ cont ->
        if(isLocal){
            localMemoDataRepository.getMemo(uniqueId){
                cont.resume(it)
            }
        }else{
            remoteMemoDataRepository.getMemo(uniqueId){
                cont.resume(it)
            }
        }
    }

    suspend fun getDraw(uniqueId: String,isLocal: Boolean):List<CheckItem> = suspendCoroutine{ cont ->
        if(isLocal){
            localMemoDataRepository.getDraw(uniqueId){
                cont.resume(it)
            }
        }else{
            remoteMemoDataRepository.getDraw(uniqueId){
                cont.resume(it)
            }
        }
    }

    suspend fun getTitle(uniqueId: String,isLocal: Boolean): String = suspendCoroutine{ cont ->
        if(isLocal){
            localMemoDataRepository.getTitle(uniqueId){
                cont.resume(it)
            }
        }else{
            remoteMemoDataRepository.getTitle(uniqueId){
                cont.resume(it)
            }
        }
    }

    fun getMemoDatas(uniqueId: String, isLocal:Boolean){
        val datas = HashMap<String,Any>()
        viewModelScope.launch {
            datas.apply {
                listOf(
                    async { put("title",getTitle(uniqueId,isLocal)) }
                    ,async { put("settings",getAlarmSetting(uniqueId,isLocal)) }
                    ,async { put("memo",getMemo(uniqueId,isLocal)) }
                    ,async { put("draw",getDraw(uniqueId,isLocal)) }
                ).awaitAll()
            }
        }.invokeOnCompletion {
            Log.d("데이터",datas.toString())
            _uiState.value = if(datas!=null&&datas["settings"]!=AlarmSetting()) UiState.Success(datas) else UiState.Failure("fail to load")
        }

    }
}