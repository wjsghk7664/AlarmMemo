package com.team5.alarmmemo.presentation.memoList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.team5.alarmmemo.R

class ListViewModel : ViewModel() {
    private val _sampleData = MutableLiveData<List<ListItem>>()
    val sampleData: LiveData<List<ListItem>> get() = _sampleData

    private var number = 0

    fun addSampleItem() {
        val item = ListItem(
            number = number++,
            title = "새 메모",
            date = "2024-08-28",
            image = R.drawable.memo_thumbnail_bg
        )
        val itemList = _sampleData.value?.toMutableList() ?: mutableListOf()
        itemList.add(item)
        _sampleData.value = itemList
    }
}