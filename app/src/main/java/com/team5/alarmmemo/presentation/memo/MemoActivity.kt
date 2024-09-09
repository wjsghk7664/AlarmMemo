package com.team5.alarmmemo.presentation.memo

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Rect
import android.icu.util.Calendar
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.team5.alarmmemo.UiState
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.team5.alarmmemo.R
import com.team5.alarmmemo.presentation.alarm.LocationService
import com.team5.alarmmemo.data.model.AlarmSetting
import com.team5.alarmmemo.databinding.ActivityMemoBinding
import com.team5.alarmmemo.util.DpPxUtil.pxToDp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MemoActivity : AppCompatActivity(), OnMapReadyCallback{

    val binding by lazy {
        ActivityMemoBinding.inflate(layoutInflater)
    }

    @Inject lateinit var colorpickerDialog: showColorpickerDialog

    private val searchViewModel:SearchAddressViewModel by viewModels()
    private val viewModel:MemoViewModel by viewModels()

    private lateinit var uniqueId:String
    private lateinit var userId:String

    private var isOriginLocation = true

    private lateinit var onTranslationTouchListener: TranslationTouchListener

    private var isLoading = true

    private val fontList = List(61){it+4}
    private val pencilList = List(10){it+1}

    private lateinit var weeks:List<TextView>

    private var isPickerLaunched =false

    var scaleRatio = 1f

    private var isLocal:Boolean? = null
    private var isInit:Boolean? =null





    private var animatorSet: AnimatorSet? =null

    @Volatile
    var init = true

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener(){

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            with(binding.memoMv){
                addDrawActive = false
                eraserActive =false
                isScaling = true
            }


            binding.memoLlContainer.pivotX = detector.focusX
            binding.memoLlContainer.pivotY = detector.focusY
            Log.d("트랜슬레이션 시작","${detector.focusX},${detector.focusY},height")



            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {

            scaleRatio *=detector.scaleFactor
            scaleRatio = scaleRatio.coerceIn(0.75f,5.0f)

            Log.d("메모 스케일", scaleRatio.toString())




            binding.memoLlContainer.scaleX=scaleRatio
            binding.memoLlContainer.scaleY=scaleRatio

            binding.memoMv.textboxMenu.root.apply {
                scaleX = 1f/scaleRatio
                scaleY = 1f/scaleRatio
            }
            binding.memoMv.bitmapMenu.root.apply {
                scaleX = 1f/scaleRatio
                scaleY = 1f/scaleRatio
            }


            Log.d("트랜슬레이션 포커스", detector.focusY.toString())
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            binding.memoMv.isScaling = false
        }

    }






    //툴 컨테이너가 아닌 영역에서 시작해야 모션 이벤트 감지가능
    private var innerFlag = false
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if(!checkToolContainer(ev?.x,ev?.y)&&ev?.action==MotionEvent.ACTION_DOWN){
            innerFlag = true
        }
        if(checkToolContainer(ev?.x,ev?.y)&&ev?.action==MotionEvent.ACTION_DOWN){
            innerFlag = false
        }
        if(!innerFlag){
            onTranslationTouchListener.onTouch(binding.memoLlContainer,ev)
        }

        return super.dispatchTouchEvent(ev)
    }

    //툴 컨테이너가 포함중이면 false반환
    private fun checkToolContainer(x:Float?,y:Float?):Boolean{
        if(x==null||y==null) return false
        val region1 = Rect()
        val region2 = Rect()
        val region3 = Rect()

        binding.memoLlToolContainer.getGlobalVisibleRect(region1)
        binding.memoLlTitleContainer.getGlobalVisibleRect(region2)
        binding.memoLlAlarmContainer.getGlobalVisibleRect(region3)



        return if(region1.contains(x.toInt(),y.toInt())||region2.contains(x.toInt(),y.toInt())||region3.contains(x.toInt(),y.toInt())) false else true
    }


    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var initFlag = false
    private var isPrevMultiTouch = false
    private var isPrevSingleTouch = false

    var touchInitY=Float.MAX_VALUE
    var touchInitTime = 0L
    var touchEndY = Float.MAX_VALUE
    var touchEndTime = 0L
    var isScrolling = false

    var isAnimationOrigin = false

    private inner class TranslationTouchListener: OnTouchListener{

        override fun onTouch(v: View, event: MotionEvent?): Boolean {

            event?.let {
                if(event.action ==MotionEvent.ACTION_DOWN&&!isAnimationOrigin) animatorSet?.cancel()


                if(event.pointerCount>1){
                    isScrolling = false
                }
                else if(event.action == MotionEvent.ACTION_MOVE&&event.pointerCount==1&&!isScrolling){
                    Log.d("메모 애니메이션 무브","1")
                    touchInitY = event.y
                    touchInitTime = System.currentTimeMillis()
                    isScrolling = true
                }else if(event.action ==MotionEvent.ACTION_UP&&event.pointerCount==1&&!binding.memoMv.drawActivate){
                    isScrolling = false
                    touchEndY = event.y
                    touchEndTime = System.currentTimeMillis()

                    val length = ((touchEndY-touchInitY)*3000f/(touchEndTime - touchInitTime).toFloat()).coerceIn(-1500F..1500F)

                    Log.d("메모 애니메이션", "시작/ ${length}")

                    if(Math.abs(length)>200&&(touchEndTime-touchInitTime)<150){
                        smoothMove(v,targetY = v.translationY + length, isOrigin = false)
                        return true
                    }


                }





                if(!binding.memoMv.drawActivate&&event.action == MotionEvent.ACTION_MOVE&&event.pointerCount==1){
                    isPrevSingleTouch = true
                    if(!initFlag||isPrevMultiTouch){
                        initFlag = true
                        isPrevMultiTouch=false
                        lastTouchX = event.x
                        lastTouchY = event.y
                    }else{
                        var dx = event.x- lastTouchX
                        var dy = event.y - lastTouchY

                        //Log.d("메모 offset","${binding.memoSvContainer.scrollX},${binding.memoSvContainer.scrollY}")

                        v.translationX+=dx
                        v.translationY+=dy

                        if(isOriginLocation){
                            isOriginLocation = false
                            binding.memoFbtnOriginlocation.setImageResource(R.drawable.ic_location_empty)
                        }


                        lastTouchX = event.x
                        lastTouchY = event.y
                    }

                }
                else if(event.pointerCount>1){
                    isPrevMultiTouch= true
                    binding.memoMv.requestLayout()
                    //binding.memoSvContainer.isScrollable = true
                    if(!initFlag||isPrevSingleTouch){
                        isPrevSingleTouch=false
                        initFlag = true
                        lastTouchX =(event.getX(0)+event.getX(1))/2
                        lastTouchY = (event.getY(0)+event.getY(1))/2
                    }else if(event.action == MotionEvent.ACTION_MOVE){
                        var dx = (event.getX(0)+event.getX(1))/2- lastTouchX
                        var dy = (event.getY(0)+event.getY(1))/2 - lastTouchY

                        v.translationX+=dx
                        v.translationY+=dy

                        if(isOriginLocation){
                            isOriginLocation = false
                            binding.memoFbtnOriginlocation.setImageResource(R.drawable.ic_location_empty)
                        }

                        lastTouchX = (event.getX(0)+event.getX(1))/2
                        lastTouchY = (event.getY(0)+event.getY(1))/2
                    }
                }else{
                    isPrevMultiTouch=false
                    isPrevSingleTouch=false
                    initFlag =false
                    //if(binding.memoIvWriteDrawSelector.isSelected) binding.memoSvContainer.isScrollable = false
                }


                if(event.action == MotionEvent.ACTION_DOWN){
                    val x = event.x
                    val y = event.y

                    val location = IntArray(2)
                    binding.memoMv.getLocationOnScreen(location)
                    val rect = Rect(location[0],location[1],location[0]+binding.memoMv.width,location[1]+binding.memoMv.height)
                    if(!rect.contains(x.toInt(),y.toInt())&&!binding.memoMv.modifyTextActivate){
                        binding.memoMv.removeActivate()
                    }
                }
            }
            return true
        }

    }

    private fun adjustView(keypadHeight:Int){
        val layoutParams = binding.memoLlToolContainer.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.bottomMargin = keypadHeight - systemBars.bottom
        binding.memoLlToolContainer.layoutParams = layoutParams
    }

    private fun resetView(){
        val layoutParams = binding.memoLlToolContainer.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.bottomMargin = 0
        binding.memoLlToolContainer.layoutParams = layoutParams
    }




    private lateinit var naverMap: NaverMap
    private var cur = LatLng(0.0,0.0)
    private var marker = Marker()
    private var isSearchAddress = false

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        cur = naverMap.cameraPosition.target
        marker.position = cur
        marker.map = naverMap
        
        naverMap.addOnCameraChangeListener { i, b ->
            cur = naverMap.cameraPosition.target
            marker.position = cur
        }

        naverMap.addOnCameraIdleListener {
            if(isSearchAddress){
                isSearchAddress=false
                return@addOnCameraIdleListener
            }
            searchViewModel.searchByLatLng(cur)
        }
        
    }


    private lateinit var mapFragment: MapFragment
    private lateinit var systemBars: Insets

    private var isFirstCheckPass = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        backgroundPermission = checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)==PackageManager.PERMISSION_GRANTED
        checkPermission()
        if(Build.VERSION.SDK_INT>=31&&!(getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms())
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:"+this@MemoActivity.packageName)
            })


        isLocal = intent.getBooleanExtra("isLocal",false)
        isInit =intent.getBooleanExtra("isInit",false)
        userId = intent.getStringExtra("userId")?:"default"
        uniqueId = intent.getStringExtra("uniqueId")?:"default"


        viewModel.setInitalSetting(userId)

        if(isInit?:true){
            viewModel.saveTitle("",uniqueId)
        }



        mapFragment = supportFragmentManager.findFragmentById(binding.memoFcvMap.id) as MapFragment?
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().add(binding.memoFcvMap.id,it).commit()
            }
        mapFragment.getMapAsync(this)
        getSearchAddress()
        onTranslationTouchListener = TranslationTouchListener()
        initView()
    }





    fun getSearchAddress(){
        lifecycleScope.launch {
            searchViewModel.uiState.collectLatest {
                when(it){
                    is UiState.Success -> {
                        withContext(Dispatchers.Main){
                            cur = it.data.first
                            val cameraZoom = naverMap.cameraPosition.zoom
                            naverMap.cameraPosition = CameraPosition(cur,cameraZoom)
                            binding.memoEtSelectLocation.setText(it.data.second)
                        }

                    }
                    else ->{}
                }
            }
        }
    }

    private fun setAlarm(alarmSetting: AlarmSetting) = with(binding){
        val times = alarmSetting.time.split("_")

        memoCbAlarmOn.isChecked = alarmSetting.isAlarmOn
        memoSpSettingCategory.setSelection(alarmSetting.alarmType)
        memoCbReminderTime.isChecked = alarmSetting.isTimeRepeat
        memoEtTimesetting.setText(alarmSetting.term.toString())
        memoEtNumsetting.setText(alarmSetting.num.toString())
        memoCbReminderOnNoti.isChecked = alarmSetting.isOnRepeat
        memoCbReminderOn.isChecked = alarmSetting.isOnMemoRepeat
        memoEtTimeHour.setText(times[0])
        memoEtTimeMinute.setText(times[1])
        memoCbTimeWeekly.isChecked = alarmSetting.isWeeklyOn
        weeks.forEachIndexed {idx,v -> v.isSelected = alarmSetting.week[idx] }
        memoCbTimeDate.isChecked = alarmSetting.isDateOn
        memoTvDate.setText(alarmSetting.date)
        cur = alarmSetting.latLng
        memoEtSelectLocation.setText(alarmSetting.address)
    }

    fun initView() = with(binding){
        root.setOnTouchListener{ view, motionEvent ->
            if(motionEvent.action == MotionEvent.ACTION_DOWN){
                val curView = currentFocus
                Log.d("메모 포커스 체크",curView.toString())
                if(currentFocus is EditText&&curView!=memoMv.textMain){
                    curView?.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(curView?.windowToken,0)
                }



            }
            false
        }

        root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener{
            override fun onGlobalLayout() {
                val rect = Rect()
                root.getWindowVisibleDisplayFrame(rect)
                val height = root.height
                val keypadHeight = height - rect.bottom

                if(keypadHeight>height*0.15){
                    adjustView(keypadHeight)
                }else{
                    resetView()
                }
            }

        })

        memoIvBack.setOnClickListener {
            finish()
        }

        weeks = listOf(memoTvWeekMon,memoTvWeekTue,memoTvWeekWed,memoTvWeekThu,memoTvWeekFri,memoTvWeekSat,memoTvWeekSun)
        if(isInit!!){
            memoProgressbar.visibility = View.GONE
        }else{
            viewModel.getMemoDatas(uniqueId, isLocal!!)
        }
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when(state){
                    is UiState.Success ->{
                        memoProgressbar.visibility = View.GONE
                        with(state.data){
                            val title = get("title") as String
                            val alarmSetting = get("settings") as AlarmSetting
                            val memo = get("memo") as SpannableStringBuilder
                            val draw = get("draw") as List<CheckItem>

                            Log.d("타이틀 세팅",title)
                            memoEtTitle.setText(title)
                            memoMv.apply {
                                Log.d("드로우리스트",draw.toString())
                                initialMemo(memo,draw)
                            }
                            setAlarm(alarmSetting)
                            viewModel.saveAlarmSetting(alarmSetting,uniqueId)
                            isLoading = false

                        }
                    }
                    is UiState.Init -> memoProgressbar.visibility = View.GONE
                    is UiState.Failure -> {
                        memoProgressbar.visibility = View.GONE
                        isLoading = false
                        Toast.makeText(this@MemoActivity, state.e, Toast.LENGTH_SHORT).show()
                    }
                    else -> null
                }
            }
        }



        weeks.forEach {
            it.setOnClickListener {
                it.isSelected = !it.isSelected
            }
        }


        var isUpdateH= false
        var isUpdateM = false

        val calendar = Calendar.getInstance()
        val cyear = calendar.get(Calendar.YEAR)
        val cmonth = calendar.get(Calendar.MONTH)+1
        val cday = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val min = calendar.get(Calendar.MINUTE)


        if(isInit?:true){
            memoEtTimeHour.setHint(String.format("%02d",hour))
            memoEtTimeHour.setText(String.format("%02d",hour))
            memoEtTimeMinute.setHint(String.format("%02d",min))
            memoEtTimeMinute.setText(String.format("%02d",min))
            memoTvDate.setText(String.format("%04d. %02d. %02d",cyear,cmonth,cday))
        }


        memoEtTimeHour.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                return
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                return
            }

            override fun afterTextChanged(s: Editable?) {
                if(!isUpdateH){
                    isUpdateH = true
                    s?.let{
                        if(it.toString().isNotEmpty()&&it.toString().toInt() !in 0..23){
                            it.replace(0,it.length,it.toString().toInt().coerceIn(0..23).let { String.format("%02d",it) })
                        }
                    }
                    isUpdateH = false
                }
            }
        })

        memoEtTimeMinute.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                return
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                return
            }

            override fun afterTextChanged(s: Editable?) {
                if(!isUpdateM){
                    isUpdateM = true
                    s?.let{
                        if(it.toString().isNotEmpty()&&it.toString().toInt() !in 0..59){
                            it.replace(0,it.length,(it.toString().toInt().coerceIn(0..59)).let { String.format("%02d",it) })
                        }
                    }
                    isUpdateM = false
                }
            }

        })

        memoTvSelectAddress.setOnClickListener {
            SearchAddressFragment.newInstance().show(supportFragmentManager,null)
        }

        memoTvSelectTime.setOnClickListener {
            val nhour = memoEtTimeHour.let{
                if(it.text.toString().isEmpty()) hour else it.text.toString().toInt()
            }
            val nmin = memoEtTimeMinute.let{
                if(it.text.toString().isEmpty()) min else it.text.toString().toInt()
            }


            val dialog = CustomTimePickerFragment.newInstance(nhour,nmin)
            dialog.setOnTimeSetListener(object :CustomTimePickerFragment.OnTimeSetListener{
                override fun onTimeSet(hour: Int, min: Int) {
                    memoEtTimeHour.setText(hour.let { String.format("%02d",it) })
                    memoEtTimeMinute.setText(min.let { String.format("%02d",it) })
                }

            })
            dialog.show(supportFragmentManager,null)
        }

        memoLlDateContainer.setOnClickListener{
            val date = memoTvDate.text.toString().split(". ").map{it.toInt()}
            val dialog = CustomDatePickerFragment.newInstance(date[0],date[1],date[2])
            dialog.setOnDateSetListener(object :CustomDatePickerFragment.OnDateSetListener{
                override fun onDateSet(year: Int, month: Int, day: Int) {
                    memoTvDate.setText(String.format("%04d. %02d. %02d",year,month,day))
                }

            })
            dialog.show(supportFragmentManager,null)
        }

        fun saveSetting(){
            val shour = memoEtTimeHour.text.toString().let{
                if(it.isEmpty()) hour.toString() else it
            }

            val smin = memoEtTimeMinute.text.toString().let{
                if(it.isEmpty()) min.toString() else it
            }

            val alarmSetting = AlarmSetting(
                memoCbAlarmOn.isChecked,
                memoSpSettingCategory.selectedItemPosition,
                memoCbReminderTime.isChecked,
                System.currentTimeMillis(),
                memoEtTimesetting.text.toString().let{
                    if(it.isEmpty()) 0 else it.toInt()
                },
                memoEtNumsetting.text.toString().let{
                    if(it.isEmpty()) 0 else it.toInt()
                },
                memoCbReminderOnNoti.isChecked,
                memoCbReminderOn.isChecked,
                "${shour.toInt().coerceIn(0..23)}_${smin.toInt().coerceIn(0..59)}",
                memoCbTimeWeekly.isChecked,
                weeks.map{it.isSelected}.toBooleanArray(),
                memoCbTimeDate.isChecked,
                memoTvDate.text.toString(),
                cur,
                memoEtSelectLocation.text.toString()
            )
            Log.d("알람세팅",alarmSetting.toString())
            CoroutineScope(Dispatchers.IO).launch {
                viewModel.saveAlarmSetting(alarmSetting, uniqueId)
            }.invokeOnCompletion {
                startForegroundService()
            }

        }

        isInit?.let{
            if(it){
                saveSetting()
            }
        }




        memoTvSettingSave.setOnClickListener {
            saveSetting()
            Toast.makeText(this@MemoActivity, "알림 설정이 저장되었습니다.", Toast.LENGTH_SHORT).show()
        }


        memoEtAddTextBox.keyListener = null
        memoEtAddTextBox.visibility = View.GONE

        memoLlContainer.translationZ = -1f

        memoLlContainer.scaleDetector = ScaleGestureDetector(binding.memoLlContainer.context,ScaleListener())

        memoLlContainer.isScrollable =false



        val photoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()){uri ->
            uri?.let {
                memoMv.addBitmap(uri)
            }
            isPickerLaunched = false
        }

        val settingCls = listOf(memoClAdditionalAlarmSettingReminder,memoClAdditionalAlarmSettingTime,memoClAdditionalAlarmSettingLocation)
        memoSpSettingCategory.adapter = ArrayAdapter(this@MemoActivity, android.R.layout.simple_spinner_item,
            arrayOf("리마인더", "시간 알림", "위치 알림")
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        memoSpSettingCategory.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if(memoIvAddtionalAlarmSettingSpread.isSelected){
                    settingCls.forEach { it.visibility = View.GONE }
                    settingCls[position].visibility = View.VISIBLE
                    if(position==1){
                        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(this@MemoActivity.currentFocus?.windowToken,0)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                return
            }

        }


        memoIvAddtionalAlarmSettingSpread.apply {
            setOnClickListener {
                isSelected = !isSelected
                if(isSelected){
                    settingCls.forEach { it.visibility = View.GONE }
                    settingCls[memoSpSettingCategory.selectedItemPosition].visibility = View.VISIBLE
                    memoMlAlarmContainer.transitionToEnd()
                }else{

                    memoMlAlarmContainer.transitionToStart()

                }
            }
        }


        memoIvAddBitmap.setOnClickListener {
            if(!isPickerLaunched){
                isPickerLaunched=true
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

        }



        val inputType = memoEtTitle.keyListener
        memoEtTitle.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(!hasFocus){
                memoEtTitle.ellipsize = TextUtils.TruncateAt.END
                memoEtTitle.keyListener=null
                memoMv.outerFocusTitle = false
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken,0)
            }else{
                memoEtTitle.ellipsize = null
                if(memoEtTitle.keyListener==null){
                    memoEtTitle.keyListener = inputType
                }
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(memoEtTitle, InputMethodManager.SHOW_IMPLICIT)
                memoMv.outerFocusTitle= true
            }
        }

        memoEtTitle.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                return
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                return
            }

            override fun afterTextChanged(s: Editable?) {
                if(::uniqueId.isInitialized&&::userId.isInitialized){
                    viewModel.saveTitle(s.toString(),uniqueId)
                }else{
                    Log.d("타이틀","uninitial")
                }

            }

        })

        memoEtAddTextBox.onFocusChangeListener = View.OnFocusChangeListener{ v, hasFocus ->
            if(!hasFocus){
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken,0)
                memoMv.outerFocusTextBox = false
                memoEtAddTextBox.keyListener = null
                Log.d("메모 텍스트 내용",memoEtAddTextBox.text.toString())
                memoMv.setTextBox(memoEtAddTextBox.text.toString())
                memoEtAddTextBox.setText("")
                memoEtAddTextBox.visibility = View.GONE
            }else{
                Log.d("메모 텍스트 체크","실패")
                if(memoEtAddTextBox.keyListener==null){
                    memoEtAddTextBox.keyListener=inputType
                }
                memoMv.activateTextBox?.let{
                    memoEtAddTextBox.setText(memoMv.textList.getOrNull(it)?:"")
                }
                memoMv.outerFocusTextBox = true
            }
        }

        memoSpTextsize.apply {
            val fontAdapter= ArrayAdapter(this@MemoActivity, R.layout.spinner_item,fontList).apply {
                setDropDownViewResource(R.layout.spinner_dropdown_item)
            }

            adapter = fontAdapter
            setSelection(20)
            onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if(init) {
                        init = false
                        return
                    }
                    Log.d("텍스트 크기 설정",fontList[position].toString())
                    memoMv.setTextSize(fontList[position].toFloat())
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    return
                }

            }
        }

        memoSpPensize.apply {
            val penAdapter = ArrayAdapter(this@MemoActivity, R.layout.spinner_item,pencilList).apply {
                setDropDownViewResource(R.layout.spinner_dropdown_item)
            }
            adapter = penAdapter
            setSelection(0)
            onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    memoMv.penSize = pencilList[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    return
                }

            }
        }



        memoMv.let{ memo->

            memo.setOnMemoChangeListener(object :MemoView.OnMemoChangeListener{
                override suspend fun onMemoChange(
                    str: SpannableStringBuilder?
                ) {

                    if(::uniqueId.isInitialized&&::userId.isInitialized){
                        viewModel.saveMemo(str,uniqueId)
                    }
                }

            })

            memo.setOnDrawChangeListener(object :MemoView.OnDrawChangeListener{
                override suspend fun onDrawChange(draw: List<CheckItem>) {
                    if(::uniqueId.isInitialized&&::userId.isInitialized&&!isLoading){
                        viewModel.saveDraw(draw, uniqueId)
                    }

                }

            })

            memo.setOnModifyTextBoxListener(object :MemoView.OnModifyTextBoxListener{
                override fun onModifyTextBox() {
                    memoEtAddTextBox.visibility = View.VISIBLE
                    memoEtAddTextBox.requestFocus()
                    memo.outerFocusTextBox = true
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(memoEtAddTextBox, InputMethodManager.SHOW_IMPLICIT)
                }

            })

            memoFbtnOriginlocation.setOnClickListener {
                memo.bitmapMenu.root.apply {
                    scaleX = 1f
                    scaleY = 1f
                }

                memo.textboxMenu.root.apply {
                    scaleX = 1f
                    scaleY = 1f
                }
                smoothMove(memoLlContainer,0f,0f,1f, isOrigin = true)

                scaleRatio = 1f
                lastTouchX = 0f
                lastTouchY = 0f
                isOriginLocation = true
                memoFbtnOriginlocation.setImageResource(R.drawable.ic_location)



            }

            memoIvGoback.apply{
                imageTintList = getColorStateList(R.color.light_gray)
                isClickable = false
            }
            memoIvGoafter.apply {
                imageTintList = getColorStateList(R.color.light_gray)
                isClickable = false
            }

            memo.setOnStyleButtonNotifyListener(object : MemoView.OnStyleButtonNotifyListener{
                override fun onStyleButtonNotify(style: MemoView.StringStyle) {
                    memoIvBold.apply {
                        isSelected = style.isBold
                        if(isSelected){
                            imageTintList = getColorStateList(R.color.orange)
                        }else{
                            imageTintList = getColorStateList(R.color.black)
                        }
                    }
                    memoIvTextcolor.imageTintList = ColorStateList.valueOf(style.color)
                    memoSpTextsize.setSelection(pxToDp(style.size).toInt() - 4)
                }

            })


            memo.setOnActivateHistoryBtnListener(object :MemoView.OnActivateHistoryBtnListener{
                override fun onActivateHistoryBtn(max: Int, cur: Int) {
                    Log.d("히스토리 버튼 리스너","${max} / ${cur}")
                    memoIvGoback.imageTintList= if(cur>=0) {
                        memoIvGoback.isClickable = true
                        getColorStateList(R.color.black)
                    } else {
                        memoIvGoback.isClickable = false
                        getColorStateList(R.color.light_gray)
                    }
                    memoIvGoafter.imageTintList = if(cur<max) {
                        memoIvGoafter.isClickable = true
                        getColorStateList(R.color.black)
                    } else {
                        memoIvGoafter.isClickable = false
                        getColorStateList(R.color.light_gray)
                    }
                }

            })

            memoIvGoback.setOnClickListener {
                memo.historyGoBack()
            }

            memoIvGoafter.setOnClickListener {
                memo.historyGoAfter()
            }

            memoIvBold.apply {
                setOnClickListener {
                    isSelected = !isSelected
                    if(isSelected){
                        imageTintList = getColorStateList(R.color.orange)
                        memo.setBold(true)
                    }else{
                        imageTintList = getColorStateList(R.color.black)
                        memo.setBold(false)
                    }
                }
            }

            memoFlTextcolorContainer.setOnClickListener {
                colorpickerDialog(true,this@MemoActivity,binding)
            }

            memoIvPencil.apply {
                isSelected = true
                setOnClickListener{
                    if(!isSelected){
                        isSelected=true
                        background = getDrawable(R.drawable.selected_menu_border)
                        memoIvEraser.isSelected =false
                        memoIvEraser.background = null
                        memo.isPencil = true
                    }else{
                        colorpickerDialog(false,this@MemoActivity,binding)
                    }
                }
            }

            memoIvEraser.apply {
                isSelected =false
                setOnClickListener{
                    if(!isSelected){
                        isSelected = true
                        background = getDrawable(R.drawable.selected_menu_border)
                        memoIvPencil.isSelected = false
                        memoIvPencil.background = null
                        memo.isPencil = false
                    }
                }
            }

            memoIvTextbox.setOnClickListener {
                memoEtAddTextBox.visibility = View.VISIBLE
                memoEtAddTextBox.requestFocus()
                memo.outerFocusTextBox = true
                memo.addTextBox()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(memoEtAddTextBox, InputMethodManager.SHOW_IMPLICIT)
            }




        }

        memoIvWriteDrawSelector.apply {
            setOnClickListener{
                isSelected = !isSelected
                if(isSelected){
                    memoMv.setTextDrawMode(true)
                    memoClContainerDraw.visibility = View.VISIBLE
                    memoClContainerWrite.visibility = View.GONE
                }else{
                    memoMv.setTextDrawMode(false)
                    memoClContainerDraw.visibility = View.GONE
                    memoClContainerWrite.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun smoothMove(view:View,targetX:Float = view.translationX,targetY:Float = view.translationY, scale:Float = view.scaleX, duration:Long = 500L, isOrigin:Boolean){
        animatorSet?.cancel()

        isAnimationOrigin = isOrigin

        val animationX = ObjectAnimator.ofFloat(view,"translationX",view.translationX,targetX)
        val animationY = ObjectAnimator.ofFloat(view, "translationY",view.translationY,targetY)
        val animationSX = ObjectAnimator.ofFloat(view,"scaleX",view.scaleX,scale)
        val animationSY = ObjectAnimator.ofFloat(view, "scaleY", view.scaleY,scale)


        animatorSet=null
        animatorSet = AnimatorSet().apply {
            playTogether(animationX,animationY,animationSX,animationSY)
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addListener(object :Animator.AnimatorListener{
                override fun onAnimationStart(animation: Animator) {
                    binding.memoMv.isAnimatorActive = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    if(binding.memoFbtnOriginlocation.isPressed){
                        Log.d("클릭실행","1")
                        binding.memoFbtnOriginlocation.callOnClick()
                        binding.memoFbtnOriginlocation.isPressed = false
                    }
                    binding.memoMv.isAnimatorActive = false
                }

                override fun onAnimationCancel(animation: Animator) {
                    binding.memoMv.isAnimatorActive = false
                }

                override fun onAnimationRepeat(animation: Animator) {}

            })
        }
        animatorSet?.start()



    }



    private var backgroundPermission = false

    private fun checkPermission(isJustCheck:Boolean = false):Boolean{
        var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) permissions+= Manifest.permission.ACTIVITY_RECOGNITION
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permissions += Manifest.permission.POST_NOTIFICATIONS
        var checkFlag = false

        permissions.forEach { if(checkSelfPermission(it) == PackageManager.PERMISSION_DENIED) checkFlag=true }

        if(!isJustCheck){
            requestPermissions(permissions,0)
        }

        if(checkFlag) {
            return false
        }else{
            return true
        }
    }

    private fun checkBackgroundPermission(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        var permission = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        if(checkSelfPermission(permission[0]) == PackageManager.PERMISSION_DENIED) {
            Log.d("권한체크", "백그라운드")
            requestPermissions(permission, 1)
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 0){
            if(!checkPermission(true)) finish()
            if(checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)== PackageManager.PERMISSION_DENIED){
                backgroundPermission=false
                checkBackgroundPermission()
            }

        }else if(requestCode == 1){
            if(checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)== PackageManager.PERMISSION_DENIED){
                backgroundPermission=false
                finish()
            }else{
                backgroundPermission=true
                if(Build.VERSION.SDK_INT>=31&&!(getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms())
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:"+this@MemoActivity.packageName)
                    })
            }
        }
    }

    fun startForegroundService(){
        if(checkPermission(true)&&checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)!= PackageManager.PERMISSION_DENIED){
            if (Build.VERSION.SDK_INT >= 31 && (getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()) {
                Log.d("서비스 실행","성공")
                ContextCompat.startForegroundService(
                    this,
                    Intent(this, LocationService::class.java))
            }else if(Build.VERSION.SDK_INT <31){
                Log.d("서비스 실행","성공")
                ContextCompat.startForegroundService(
                    this,
                    Intent(this, LocationService::class.java))
            }else{
                Log.d("서비스 실행","실패")
            }
        }else{
            Log.d("서비스 실행","실패2")
        }
    }

}