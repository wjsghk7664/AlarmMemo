package com.team5.alarmmemo.data.model

data class MemoList(
    val memoLists: List<MemoUnitData> = listOf()
)

data class MemoUnitData(
    val uniqId:String="",
    val title:String="",
    val memo:String="" //json형태 데이터
)