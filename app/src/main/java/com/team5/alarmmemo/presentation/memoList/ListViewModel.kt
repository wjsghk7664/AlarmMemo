package com.team5.alarmmemo.presentation.memoList

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.team5.alarmmemo.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ListViewModel(application: Application) : AndroidViewModel(application) {
    private val _sampleData = MutableLiveData<List<ListItem>>()
    val sampleData: LiveData<List<ListItem>> get() = _sampleData

    private val _spanCount = MutableLiveData(2)
    val spanCount: LiveData<Int> get() = _spanCount

    private var number = 0

    private val sharedPreferences = application.getSharedPreferences("sharedPreferences", Application.MODE_PRIVATE)
    private val gson = GsonBuilder().create()

    // 아이템 리스트 불러오기
    fun loadList() {
        loadData()
        loadSpanCount()
    }

    // 아이템 추가하는 메소드
    fun addSampleItem() {
        val dateNow = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())

        val item = ListItem(
            number = number++,
            title = "새 메모",
            date = dateNow,
            image = R.drawable.memo_thumbnail_bg
        )
        val itemList = _sampleData.value?.toMutableList() ?: mutableListOf()
        itemList.add(item)
        _sampleData.value = itemList

        saveData(itemList)
    }

    // 아이템 리스트 저장하는 메소드
    private fun saveData(itemList: List<ListItem>) {
        val json = gson.toJson(itemList)
        sharedPreferences.edit()
            .putString("sampleData", json)
            .apply()
    }

    // 아이템 리스트 앱 재시작 시 불러오는 메소드
    private fun loadData() {
        val json = sharedPreferences.getString("sampleData", null)
        val typeToken = object : TypeToken<List<ListItem>>() {}.type
        val itemList: List<ListItem>? = gson.fromJson(json, typeToken)
        _sampleData.value = itemList ?: emptyList()
    }

    // 아이템 (썸네일) 길게 눌렀을 때 삭제하는 메소드
    fun deleteItem(item: ListItem) {
        val itemList = _sampleData.value?.toMutableList()
        itemList?.remove(item)
        _sampleData.value = itemList ?: emptyList()
        saveData(itemList ?: emptyList())
    }

    // SpanCount 업데이트 하는 메소드
    fun setSpanCount(count: Int) {
        _spanCount.value = count
        sharedPreferences.edit()
            .putInt("spanCount", count)
            .apply()
    }

    // SpanCount를 저장했던 걸 불러오는 메소드
    private fun loadSpanCount() {
        val saveSpanCount = sharedPreferences.getInt("spanCount", 2)
        _spanCount.value = saveSpanCount
    }
}
