package com.team5.alarmmemo.presentation.memoList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.team5.alarmmemo.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ListViewModel : ViewModel() {
    private val _sampleData = MutableLiveData<List<ListItem>>()
    val sampleData: LiveData<List<ListItem>> get() = _sampleData

    private val _spanCount = MutableLiveData(2)
    val spanCount: LiveData<Int> get() = _spanCount

    private var number = 0

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
    }

    fun setSpanCount(count: Int) {
        _spanCount.value = count
    }
}