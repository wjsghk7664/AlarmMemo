package com.team5.alarmmemo.presentation.alarm

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmCall(private val context: Context) {

    private lateinit var pendingIntent: PendingIntent

    fun callAlarm(alarmCode:Long, uniqueId:String, isLocal:Boolean){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val receiverIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarm_requestCode",alarmCode.toInt())

            putExtra("alarm_uniqueId",uniqueId)
            putExtra("alarm_isLocal",isLocal)
        }

        val pendingIntent =
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.getBroadcast(context, alarmCode.toInt(),receiverIntent, PendingIntent.FLAG_IMMUTABLE)
            else PendingIntent.getBroadcast(context, alarmCode.toInt(),receiverIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        if(Build.VERSION.SDK_INT>=31&&!alarmManager.canScheduleExactAlarms()){
            (context as Activity).requestPermissions(arrayOf(android.Manifest.permission.SCHEDULE_EXACT_ALARM),0)
        }else{
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmCode,pendingIntent)
        }
    }


}