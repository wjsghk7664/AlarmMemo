package com.team5.alarmmemo.presentation.alarm

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.team5.alarmmemo.R
import com.team5.alarmmemo.presentation.memo.MemoActivity

class AlarmReceiver: BroadcastReceiver() {

    private lateinit var manager: NotificationManager
    private lateinit var builder: NotificationCompat.Builder

    private val myNotificationID = 1
    private val channelID = "default"

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onReceive(context: Context?, intent: Intent?) {
        manager=context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            manager.createNotificationChannel(
                NotificationChannel(
                    channelID,
                    "default channel",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }

        builder = NotificationCompat.Builder(context,channelID)

        val intents = Intent(context, MemoActivity::class.java)
        val requestCode = intent?.extras!!.getInt("alarm_requestCode")
        val uniqueId = intent?.extras!!.getString("alarm_uniqueId")
        val userId = intent?.extras!!.getString("alarm_userId")
        val isLocal = intent?.extras!!.getBoolean("alarm_isLocal")

        intents.apply {
            putExtra("uniqueId",uniqueId)
            putExtra("userId",userId)
            putExtra("isLocal",isLocal)
            putExtra("isInit",false)
        }

        val pendingIntent =
            if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.S)
                PendingIntent.getActivity(context,requestCode,intents, PendingIntent.FLAG_IMMUTABLE)
            else PendingIntent.getActivity(context,requestCode,intents, PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = builder.setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("메모 알림!")
            .setContentText("눌러서 메모를 확인하세요.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            (context as Activity).requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),0)
            return
        }
        manager.notify(myNotificationID,notification)
    }
}