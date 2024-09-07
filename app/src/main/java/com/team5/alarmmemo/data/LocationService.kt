package com.team5.alarmmemo.data

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.team5.alarmmemo.data.model.AlarmSetting
import com.team5.alarmmemo.data.repository.memo.LocalMemoDataRepositoryImpl
import com.team5.alarmmemo.data.repository.memo.LocalRepository
import com.team5.alarmmemo.data.repository.memo.MemoDataRepository
import com.team5.alarmmemo.data.repository.memo.RemoteRepository
import com.team5.alarmmemo.data.source.local.AlarmSettings
import com.team5.alarmmemo.presentation.alarm.AlarmCall
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class LocationService : Service() {

    @Inject lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    @Inject @LocalRepository lateinit var localMemoDataRepository: MemoDataRepository
    @Inject @RemoteRepository lateinit var remoteMemoDataRepository: MemoDataRepository

    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()

        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult?.let {
                    for(location in locationResult.locations){
                        CoroutineScope(Dispatchers.IO).launch {

                        }
                    }
                }
            }
        }
    }

    fun generateAlarms(context: Context){
        val alarmSettings = ArrayList<AlarmSetting>()
        val uniqIds = ArrayList<String>()
        localMemoDataRepository.getAllAlarms { localAlarm,localIds ->
            alarmSettings+=localAlarm
            uniqIds+=localIds
            remoteMemoDataRepository.getAllAlarms { remoteAlarm,remoteIds ->
                alarmSettings+=remoteAlarm
                uniqIds+=remoteIds
                alarmSettings.forEachIndexed {idx, it ->
                    if(it.isAlarmOn){
                        when(it.alarmType){
                            0 -> {
                                val term = it.term
                                val num = it.num
                                if(term!=0&&num!=0){
                                    var init = it.curtime
                                    CoroutineScope(Dispatchers.IO).launch {
                                        AlarmCall(context).run{
                                            repeat(num){
                                                init+=(term*60*1000).toLong()
                                                if(init>System.currentTimeMillis()){
                                                    callAlarm(init,uniqIds[idx],true)
                                                }

                                            }
                                        }

                                    }

                                }

                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationUpdate()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("MissingPermission")
    private fun locationUpdate(){
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000
        ).apply {
            setMinUpdateIntervalMillis(1000)
            setWaitForAccurateLocation(false)

        }.build()
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )

    }

    private fun stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}