package com.team5.alarmmemo.presentation.memoList

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.team5.alarmmemo.R
import com.team5.alarmmemo.data.repository.lastmodify.LastModifyRepository
import com.team5.alarmmemo.data.repository.lastmodify.LocalLastModify
import com.team5.alarmmemo.data.repository.lastmodify.ReMoteLastModify
import com.team5.alarmmemo.data.repository.lastmodify.RemoteLastModifyRepositoryImpl
import com.team5.alarmmemo.data.repository.memo.LocalRepository
import com.team5.alarmmemo.data.repository.memo.MemoDataRepository
import com.team5.alarmmemo.data.repository.memo.RemoteMemoDataRepositoryImpl
import com.team5.alarmmemo.data.repository.memo.RemoteRepository
import com.team5.alarmmemo.data.source.local.SpanCount
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    private val _sampleData = MutableLiveData<ArrayList<Triple<String,String, SpannableStringBuilder>>>()
    val sampleData: LiveData<ArrayList<Triple<String,String, SpannableStringBuilder>>> get() = _sampleData

    private val _spanCount = MutableLiveData(2)
    val spanCount: LiveData<Int> get() = _spanCount

    private var number = 0

    fun setId(id:String){
        remoteMemoDataRepository as RemoteMemoDataRepositoryImpl
        remoteLastModifyRepository as RemoteLastModifyRepositoryImpl
        remoteMemoDataRepository.setUserid(id)
        remoteLastModifyRepository.setUserId(id)
    }

    // 아이템 리스트 불러오기
    fun loadList() {
        loadData()
        loadSpanCount()
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
        var list = ArrayList<Triple<String,String, SpannableStringBuilder>>()

        val isLocal = ArrayList<Boolean>()
        val lastModifyAsync = ArrayList<Deferred<Unit>>()
        val lastModify = ArrayList<Long>()

        localMemoDataRepository.getList { localList ->
            list+=localList
            isLocal+=List(localList.size){true}
            remoteMemoDataRepository.getList { remoteList ->
                list+=remoteList
                isLocal+=List(remoteList.size){false}
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



    // 아이템 (썸네일) 길게 눌렀을 때 삭제하는 메소드
    fun deleteItem(item: Triple<String,String, SpannableStringBuilder>) {
        val itemList = _sampleData.value?.toMutableList()
        itemList?.remove(item)
        _sampleData.value = (itemList ?: ArrayList()) as ArrayList<Triple<String, String, SpannableStringBuilder>>
        //TODO("삭제 로직 만들고 진행")
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
