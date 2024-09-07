package com.team5.alarmmemo.presentation.alarm

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.startForegroundService
import com.team5.alarmmemo.data.LocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

class RebootReciever: BroadcastReceiver() {



    override fun onReceive(context: Context, intent: Intent) {
        if(checkPermission(context)&&intent.action.equals("android.intent.action.BOOT_COMPLETED")){
            startForegroundService(context,Intent(context,LocationService::class.java))
        }
    }

    private fun checkPermission(context: Context):Boolean{
        var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) permissions+= Manifest.permission.ACTIVITY_RECOGNITION
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permissions += Manifest.permission.POST_NOTIFICATIONS
        var checkFlag = false

        permissions.forEach { if(checkSelfPermission(context,it) == PackageManager.PERMISSION_DENIED) checkFlag=true }

        if(checkFlag) {
            return false
        }else{
            return true
        }
    }

}