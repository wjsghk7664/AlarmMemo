package com.team5.alarmmemo.data.model

import com.naver.maps.geometry.LatLng

data class AlarmSetting(
    val isAlarmOn: Boolean = false,
    val alarmType: Int = 0,

    val isTimeRepeat: Boolean = false,
    val curtime:Long = 0L,
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
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AlarmSetting

        if (isAlarmOn != other.isAlarmOn) return false
        if (alarmType != other.alarmType) return false
        if (isTimeRepeat != other.isTimeRepeat) return false
        if (term != other.term) return false
        if (num != other.num) return false
        if (isOnRepeat != other.isOnRepeat) return false
        if (isOnMemoRepeat != other.isOnMemoRepeat) return false
        if (time != other.time) return false
        if (isWeeklyOn != other.isWeeklyOn) return false
        if(week.size!=other.week.size) {
            return false
        } else{
            for(i in week.indices){
                if(week[i]!=other.week[i]) return false
            }
        }

        if (isDateOn != other.isDateOn) return false
        if (date != other.date) return false
        if (latLng != other.latLng) return false
        if (address != other.address) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isAlarmOn.hashCode()
        result = 31 * result + alarmType
        result = 31 * result + isTimeRepeat.hashCode()
        result = 31 * result + term
        result = 31 * result + num
        result = 31 * result + isOnRepeat.hashCode()
        result = 31 * result + isOnMemoRepeat.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + isWeeklyOn.hashCode()
        result = 31 * result + week.contentHashCode()
        result = 31 * result + isDateOn.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + latLng.hashCode()
        result = 31 * result + address.hashCode()
        return result
    }
}