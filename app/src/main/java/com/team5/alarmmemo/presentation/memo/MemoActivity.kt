package com.team5.alarmmemo.presentation.memo

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet.Motion
import androidx.core.graphics.Insets
import androidx.core.graphics.translationMatrix
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.team5.alarmmemo.R
import com.team5.alarmmemo.databinding.ActivityMemoBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MemoActivity : AppCompatActivity() {

    val binding by lazy {
        ActivityMemoBinding.inflate(layoutInflater)
    }

    @Inject lateinit var colorpickerDialog: showColorpickerDialog


    private var isOriginLocation = true

    private lateinit var onTranslationTouchListener: TranslationTouchListener



    private val fontList = List(61){it+4}
    private val pencilList = List(10){it+1}

    private var isPickerLaunched =false

    var scaleRatio = 1f





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



    private lateinit var systemBars: Insets

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        onTranslationTouchListener = TranslationTouchListener()
        initView()
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
        val region = Rect()

        binding.memoLlToolContainer.getGlobalVisibleRect(region)

        return if(region.contains(x.toInt(),y.toInt())) false else true
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
            setSelection(4)
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
                    memoMv.setTextSize(dpToPx(this@MemoActivity,fontList[position].toFloat()))
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

//            memo.setOnStyleButtonNotifyListener(object : MemoView.OnStyleButtonNotifyListener{
//                override fun onStyleButtonNotify(style: MemoView.StringStyle) {
//                    memoIvBold.apply {
//                        isSelected = style.isBold
//                        if(isSelected){
//                            imageTintList = getColorStateList(R.color.orange)
//                        }else{
//                            imageTintList = getColorStateList(R.color.black)
//                        }
//                    }
//                    memoIvTextcolor.imageTintList = ColorStateList.valueOf(style.color)
//                    Log.d("텍스트 크기",(pxToDp(this@MemoActivity,style.size).toInt()+4).toString())
//                    init = true
//                    memoSpTextsize.setSelection(pxToDp(this@MemoActivity,style.size).toInt())
//                }
//
//            })


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


}