package com.team5.alarmmemo.data.model

import com.naver.maps.geometry.LatLng

data class AlarmSetting(
    val isAlarmOn: Boolean = false,
    val alarmType: Int = 0,

    val isTimeRepeat: Boolean = false,
    val term: Int = 1,
    val num: Int = 1,
    val isOnRepeat: Boolean = false,
    val isOnMemoRepeat: Boolean = false,

    val time: String = "",
    val isWeeklyOn: Boolean = false,
    val week: BooleanArray = booleanArrayOf(),
    val isDateOn: Boolean = false,
    val date: String = "",

    val latLng: LatLng = LatLng(0.0, 0.0),
    val address: String = ""
)