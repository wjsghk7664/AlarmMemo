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
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.team5.alarmmemo.R
import com.team5.alarmmemo.data.model.MemoUnitData
import com.team5.alarmmemo.data.repository.memo.LocalRepository
import com.team5.alarmmemo.data.repository.memo.MemoDataRepository
import com.team5.alarmmemo.data.repository.memo.RemoteMemoDataRepositoryImpl
import com.team5.alarmmemo.data.repository.memo.RemoteRepository
import com.team5.alarmmemo.data.source.local.SpanCount
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    @SpanCount private val spanSharedPreferences: SharedPreferences,
    @LocalRepository private val localMemoDataRepository: MemoDataRepository,
    @RemoteRepository private val remoteMemoDataRepository: MemoDataRepository
) : ViewModel() {
    private val _sampleData = MutableLiveData<ArrayList<Triple<String,String, SpannableStringBuilder>>>()
    val sampleData: LiveData<ArrayList<Triple<String,String, SpannableStringBuilder>>> get() = _sampleData

    private val _spanCount = MutableLiveData(2)
    val spanCount: LiveData<Int> get() = _spanCount

    private var number = 0

    fun setId(id:String){
        remoteMemoDataRepository as RemoteMemoDataRepositoryImpl
        remoteMemoDataRepository.setUserid(id)
    }

    // 아이템 리스트 불러오기
    fun loadList() {
        loadData()
        loadSpanCount()
    }

    // 아이템 추가하는 메소드
    //UIState추가하면 에러핸들링 코드 넣기
   fun additem(memoUnitData: MemoUnitData){
        (remoteMemoDataRepository as RemoteMemoDataRepositoryImpl).addList(memoUnitData){
            if(it==1){
                remoteMemoDataRepository.createList { isSuccess->
                    if(isSuccess){
                        remoteMemoDataRepository.addList(memoUnitData){
                            Log.d("아이템 추가","성공")
                        }
                    }
                }
            }
        }
   }


    // 아이템 리스트 앱 재시작 시 불러오는 메소드
    fun loadData(){
        val list = ArrayList<Triple<String,String, SpannableStringBuilder>>()
        localMemoDataRepository.getList { localList ->
            list+=localList
            remoteMemoDataRepository.getList { remoteList ->
                list+=remoteList
                _sampleData.value = list
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
