package com.team5.alarmmemo.presentation.memoList

import android.content.SharedPreferences
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team5.alarmmemo.UiState
import com.team5.alarmmemo.data.repository.lastmodify.LastModifyRepository
import com.team5.alarmmemo.data.repository.lastmodify.LocalLastModify
import com.team5.alarmmemo.data.repository.lastmodify.ReMoteLastModify
import com.team5.alarmmemo.data.repository.lastmodify.RemoteLastModifyRepositoryImpl
import com.team5.alarmmemo.data.repository.memo.LocalRepository
import com.team5.alarmmemo.data.repository.memo.MemoDataRepository
import com.team5.alarmmemo.data.repository.memo.RemoteMemoDataRepositoryImpl
import com.team5.alarmmemo.data.repository.memo.RemoteRepository
import com.team5.alarmmemo.data.source.local.SpanCount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class ListViewModel @Inject constructor(
    @SpanCount private val spanSharedPreferences: SharedPreferences,
    @LocalRepository private val localMemoDataRepository: MemoDataRepository,
    @RemoteRepository private val remoteMemoDataRepository: MemoDataRepository,
    @LocalLastModify private val localLastModifyRepository: LastModifyRepository,
    @ReMoteLastModify private val remoteLastModifyRepository: LastModifyRepository

) : ViewModel() {
    private val _sampleData = MutableStateFlow<List<Triple<String, String, SpannableStringBuilder>>>(emptyList())
    val sampleData: StateFlow<List<Triple<String, String, SpannableStringBuilder>>> get() = _sampleData

    private val _uiState = MutableStateFlow<UiState<List<Triple<String, String, SpannableStringBuilder>>>>(UiState.Init)
    val uiState: StateFlow<UiState<List<Triple<String, String, SpannableStringBuilder>>>> get() = _uiState

    private val _spanCount = MutableStateFlow(2)
    val spanCount: StateFlow<Int> get() = _spanCount

    private var number = 0

    fun setId(id:String){
        remoteMemoDataRepository as RemoteMemoDataRepositoryImpl
        remoteLastModifyRepository as RemoteLastModifyRepositoryImpl
        remoteMemoDataRepository.setUserid(id)
        remoteLastModifyRepository.setUserId(id)
    }

    // 아이템 리스트 불러오기
    fun loadList() {
        viewModelScope.launch {
            loadData()
            loadSpanCount()
            _uiState.value = UiState.Success(_sampleData.value)
        }
    }


    //Load에서 바로 수정하기. filtering은 지우고
    suspend fun getLastModify(uniqueId: String, isLocal: Boolean): Long = suspendCoroutine { cont ->
        if(isLocal){
            localLastModifyRepository.getLastModifyTime(uniqueId){
                cont.resume(it)
            }
        }else{
            remoteLastModifyRepository.getLastModifyTime(uniqueId){
                cont.resume(it)
            }
        }
    }

    // 아이템 리스트 앱 재시작 시 불러오는 메소드
    fun loadData(){
        Log.d("로드 데이터 시작","")
        var list = ArrayList<Triple<String,String, SpannableStringBuilder>>()

        val isLocal = ArrayList<Boolean>()
        val lastModifyAsync = ArrayList<Deferred<Unit>>()
        val lastModify = ArrayList<Long>()

        localMemoDataRepository.getList { localList ->
            list+=localList
            isLocal+=List(localList.size){true}
            Log.d("데이터 - 로컬",list.toString())
            remoteMemoDataRepository.getList { remoteList ->
                list+=remoteList
                isLocal+=List(remoteList.size){false}
                Log.d("데이터 - 리모트",list.toString())
                viewModelScope.launch {
                    for(i in isLocal.indices){
                        lastModifyAsync+=async { lastModify+=getLastModify(list[i].first,isLocal[i]) }
                    }
                    lastModifyAsync.awaitAll()

                }.invokeOnCompletion {
                    val ids = HashMap<String,Long>()
                    for(i in isLocal.indices){
                        val item = list[i]
                        if(!ids.keys.contains(item.first)){
                            ids.put(item.first, lastModify[i])
                        }else if(lastModify[i] > ids[item.first]!!){
                            ids[item.first] = lastModify[i]
                        }
                    }
                    Log.d("데이터 중복제거전",ids.toString())
                    val data = ArrayList(list.filter { ids.keys.contains(it.first) }.distinctBy { it.first })
                    Log.d("직전 데이터",data.toString())
                    _sampleData.value = data
                }
            }
        }
    }

    suspend fun removeDraw(uniqueId: String):Boolean = suspendCoroutine { cont ->
        localMemoDataRepository.removeDraw(uniqueId){ local ->
            if(local){
                remoteMemoDataRepository.removeDraw(uniqueId){
                    cont.resume(it)
                }
            }else{
                cont.resume(local)
            }
        }
    }
    suspend fun removeAlarm(uniqueId: String):Boolean = suspendCoroutine { cont ->
        localMemoDataRepository.removeAlarmSetting(uniqueId){ local ->
            if(local){
                remoteMemoDataRepository.removeAlarmSetting(uniqueId){
                    cont.resume(it)
                }
            }else{
                cont.resume(local)
            }
        }
    }
    suspend fun removeMemo(uniqueId: String):Boolean = suspendCoroutine { cont ->
        localMemoDataRepository.removeMemo(uniqueId){ local ->
            if(local){
                remoteMemoDataRepository.removeMemo(uniqueId){
                    cont.resume(it)
                }
            }else{
                cont.resume(local)
            }
        }
    }
    suspend fun removeTitle(uniqueId: String):Boolean = suspendCoroutine { cont ->
        localMemoDataRepository.removeTitle(uniqueId){ local ->
            if(local){
                remoteMemoDataRepository.removeTitle(uniqueId){
                    cont.resume(it)
                }
            }else{
                cont.resume(local)
            }
        }
    }

    // 아이템 (썸네일) 길게 눌렀을 때 삭제하는 메소드
    fun deleteItem(item: Triple<String,String, SpannableStringBuilder>) {
        val itemList = _sampleData.value?.toMutableList()
        val uniqueId = item.first
        itemList?.remove(item)
        viewModelScope.launch {
            val list = listOf(
                async { removeAlarm(uniqueId) },
                async { removeMemo(uniqueId) },
                async { removeTitle(uniqueId) },
                async { removeDraw(uniqueId) }
            )
            val isSuccessAll = list.awaitAll().all { it }

            if(isSuccessAll){
                _sampleData.value = (itemList ?: ArrayList()) as ArrayList<Triple<String, String, SpannableStringBuilder>>
            }

        }
    }

    // SpanCount 업데이트 하는 메소드
    fun setSpanCount(count: Int) {
        _spanCount.value = count
        spanSharedPreferences.edit()
            .putInt("spanCount", count)
            .apply()
    }

    // SpanCount를 저장했던 걸 불러오는 메소드
    private fun loadSpanCount() {
        val saveSpanCount = spanSharedPreferences.getInt("spanCount", 2)
        _spanCount.value = saveSpanCount
    }
}
