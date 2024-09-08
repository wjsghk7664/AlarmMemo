package com.team5.alarmmemo.presentation.alarm

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.naver.maps.geometry.LatLng
import com.team5.alarmmemo.R
import com.team5.alarmmemo.data.model.AlarmSetting
import com.team5.alarmmemo.data.repository.AlarmRepository
import com.team5.alarmmemo.data.repository.memo.LocalRepository
import com.team5.alarmmemo.data.repository.memo.MemoDataRepository
import com.team5.alarmmemo.data.repository.memo.RemoteRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

//알람 바뀔때 마다 재실행
@AndroidEntryPoint
class LocationService : Service() {

    @Inject lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    @LocalRepository @Inject lateinit var localMemoDataRepository: MemoDataRepository
    @RemoteRepository @Inject lateinit var remoteMemoDataRepository: MemoDataRepository
    @Inject lateinit var alarmRepository: AlarmRepository

    private lateinit var locationCallback: LocationCallback

    val alarmLocationList = HashMap<Triple<LatLng,String,String>,Pair<Boolean,Boolean>>() //isLocal, isActive

    var curActiveAlarms = HashSet<Triple<Long,String,Boolean>>()

    companion object {
        const val CHANNEL_ID = "ForegroundServiceChannel"
        const val NOTIFICATION_ID = 2
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("서비스","시작")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        // 알림 빌드
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("위치기반 알림 서비스")
            .setContentText("실행중...")
            .setSmallIcon(R.drawable.ic_logo) // 작은 아이콘 설정
            .build()

        // 포그라운드 서비스로 시작
        startForeground(NOTIFICATION_ID, notification)

        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult?.let {
                    for(location in locationResult.locations){
                        CoroutineScope(Dispatchers.IO).launch {
                            Log.d("서비스",alarmLocationList.toString())
                            for((k,v) in alarmLocationList){
                                val latLng = k.first
                                val distance = latLngDistance(location.latitude,location.longitude,latLng.latitude,latLng.longitude)
                                Log.d("서비스 거리",distance.toString())
                                //70미터 바깥이면 초기화, 50미터 이내면 알림
                                if(distance<=50&&!v.second){
                                    val isLocal = alarmLocationList[k]?.first?:true
                                    alarmLocationList[k]=Pair(isLocal,true)
                                    AlarmCall(this@LocationService).callAlarm(System.currentTimeMillis()+5000L,k.third,isLocal)
                                }
                                if(distance>70){
                                    val isLocal = alarmLocationList[k]?.first?:true
                                    alarmLocationList[k]=Pair(isLocal,false)
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun latLngDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3 // Radius of Earth in meters
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLon = Math.toRadians(lon2 - lon1)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLon / 2) * sin(deltaLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }

    fun convertToMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int): Long {
        val dateTime = LocalDateTime.of(year, month, day, hour, minute, second)
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    fun generateAlarms(context: Context){
        val alarmSettings = ArrayList<AlarmSetting>()
        val uniqIds = ArrayList<String>()
        val isLocal = ArrayList<Boolean>()
        val registeredActiveAlarms = alarmRepository.getAlarms()




        val activeAlarms = HashSet<Triple<Long,String,Boolean>>()
        localMemoDataRepository.getAllAlarms { localAlarm,localIds ->
            alarmSettings+=localAlarm
            uniqIds+=localIds
            isLocal+=BooleanArray(localIds.size){true}.toList()
            remoteMemoDataRepository.getAllAlarms { remoteAlarm,remoteIds ->
                alarmSettings+=remoteAlarm
                uniqIds+=remoteIds
                isLocal+=BooleanArray(remoteIds.size){false}.toList()

                Log.d("알람목록",alarmSettings.toString())

                alarmSettings.forEachIndexed {idx, it ->
                    if(it.isAlarmOn){
                        when(it.alarmType){
                            //TODO("0-=리마인더:화면 켤 떄마다, 화면 켤떄마다 메모 구현하기")
                            0 -> {
                                if(it.isTimeRepeat){
                                    val term = it.term
                                    val num = it.num
                                    if(term!=0&&num!=0){
                                        var init = it.curtime
                                        AlarmCall(context).run{
                                            repeat(num){
                                                activeAlarms+=Triple(init,uniqIds[idx],isLocal[idx])
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    init+=(term*60*1000).toLong()
                                                    if(init>System.currentTimeMillis()){
                                                        callAlarm(init,uniqIds[idx],isLocal[idx])
                                                    }
                                                }

                                            }
                                        }

                                    }
                                }
                                if(it.isOnRepeat){

                                }
                                if(it.isOnMemoRepeat){

                                }


                            }
                            1->{
                                if(it.isDateOn){
                                    val dateList = it.date.split(". ").map { it.toInt() }
                                    val timeList = it.time.split("_").map{it.toInt()}
                                    val date = convertToMillis(dateList[0],dateList[1],dateList[2],timeList[0],timeList[1],0)
                                    if(date>System.currentTimeMillis()){
                                        activeAlarms+=Triple(date,uniqIds[idx],isLocal[idx])
                                        CoroutineScope(Dispatchers.IO).launch {
                                            AlarmCall(context).callAlarm(date,uniqIds[idx],isLocal[idx])

                                        }
                                    }
                                }
                                if(it.isWeeklyOn){
                                    val timeList = it.time.split("_").map { it.toInt() }
                                    for(i in 0..6){
                                        if(it.week[i]){
                                            val weekInt = when(i){
                                                0 -> Calendar.MONDAY
                                                1 -> Calendar.TUESDAY
                                                2 -> Calendar.WEDNESDAY
                                                3 -> Calendar.THURSDAY
                                                4 -> Calendar.FRIDAY
                                                5 -> Calendar.SATURDAY
                                                6 -> Calendar.SUNDAY
                                                else -> Calendar.SUNDAY
                                            }
                                            val calendar = Calendar.getInstance().apply {
                                                set(Calendar.DAY_OF_WEEK, weekInt)
                                                set(Calendar.HOUR_OF_DAY, timeList[0])
                                                set(Calendar.MINUTE, timeList[1])
                                                set(Calendar.SECOND, 0)
                                                set(Calendar.MILLISECOND, 0)
                                                if (timeInMillis < System.currentTimeMillis()) {
                                                    add(Calendar.WEEK_OF_YEAR, 1) // 현재 시간이 이미 지나간 경우 다음 주로 설정
                                                }
                                            }

                                            activeAlarms+=Triple(calendar.timeInMillis,uniqIds[idx],isLocal[idx])
                                            CoroutineScope(Dispatchers.IO).launch {
                                                AlarmCall(context).callWeeklyAlarm(calendar,uniqIds[idx],isLocal[idx])
                                            }
                                        }
                                    }
                                }
                            }
                            2->{
                                alarmLocationList.put(Triple(it.latLng,it.address,uniqIds[idx]),Pair(isLocal[idx],false))
                            }
                        }
                    }
                }

                //추가는 위에서 진행했으니 취소만 여기서 처리
                val canceledAlarms = registeredActiveAlarms - activeAlarms
                CoroutineScope(Dispatchers.IO).launch {
                    canceledAlarms.forEach {
                        AlarmCall(context).cancelAlarm(it.first,it.second,it.third)
                    }
                }

                curActiveAlarms = activeAlarms
                Log.d("알람 목록",curActiveAlarms.toString())
                alarmRepository.setAlarms(curActiveAlarms)

            }

        }


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationUpdate()
        generateAlarms(this@LocationService)
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