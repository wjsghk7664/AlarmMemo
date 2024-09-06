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

    fun loadList() {
        loadData()
        loadSpanCount()
    }

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

    private fun saveData(itemList: List<ListItem>) {
        val json = gson.toJson(itemList)
        sharedPreferences.edit()
            .putString("sampleData", json)
            .apply()
    }

    private fun loadData() {
        val json = sharedPreferences.getString("sampleData", null)
        val typeToken = object : TypeToken<List<ListItem>>() {}.type
        val itemList: List<ListItem>? = gson.fromJson(json, typeToken)
        _sampleData.value = itemList ?: emptyList()
    }

    fun deleteItem(item: ListItem) {
        val itemList = _sampleData.value?.toMutableList()
        itemList?.remove(item)
        _sampleData.value = itemList ?: emptyList()
        saveData(itemList ?: emptyList())
    }

    fun setSpanCount(count: Int) {
        _spanCount.value = count
        sharedPreferences.edit()
            .putInt("spanCount", count)
            .apply()
    }

    private fun loadSpanCount() {
        val saveSpanCount = sharedPreferences.getInt("spanCount", 2)
        _spanCount.value = saveSpanCount
    }
}