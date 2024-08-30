package com.team5.alarmmemo.presentation.memo

import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
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
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginBottom
import androidx.lifecycle.lifecycleScope
import com.team5.alarmmemo.R
import com.team5.alarmmemo.databinding.ActivityMemoBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MemoActivity : AppCompatActivity() {

    val binding by lazy {
        ActivityMemoBinding.inflate(layoutInflater)
    }

    @Inject lateinit var colorpickerDialog: showColorpickerDialog


    private var cursorY = 0f


    private val fontList = List(61){it+4}
    private val pencilList = List(10){it+1}

    private var isPickerLaunched =false

    private lateinit var scaleDetector: ScaleGestureDetector
    var scaleRatio = 1f

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener(){

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            with(binding.memoMv){
                addDrawActive = false
                eraserActive =false
                isScaling = true
            }

            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleRatio *=detector.scaleFactor
            scaleRatio = scaleRatio.coerceIn(0.75f,5.0f)

            Log.d("메모 스케일", scaleRatio.toString())

            binding.memoMv.scaleX=scaleRatio
            binding.memoMv.scaleY=scaleRatio


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



        scaleDetector = ScaleGestureDetector(this,ScaleListener())
        initView()
    }

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var initFlag = false
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

        scaleDetector.onTouchEvent(ev)
        val event = ev
        if(!binding.memoMv.drawActivate&&event.action == MotionEvent.ACTION_MOVE&&event.pointerCount==1){
            if(!initFlag){
                initFlag = true
                lastTouchX = event.x
                lastTouchY = event.y
            }else{
                var dx = event.x- lastTouchX
                var dy = event.y - lastTouchY


                binding.memoMv.translationX+=dx
                binding.memoMv.translationY+=dy


                lastTouchX = event.x
                lastTouchY = event.y
            }

        }
        else if(event.pointerCount>1){
            binding.memoMv.requestLayout()
            binding.memoSvContainer.isScrollable = true
            if(!initFlag){
                initFlag = true
                lastTouchX =(event.getX(0)+event.getX(1))/2
                lastTouchY = (event.getY(0)+event.getY(1))/2
            }else if(event.action == MotionEvent.ACTION_MOVE){
                var dx = (event.getX(0)+event.getX(1))/2- lastTouchX
                var dy = (event.getY(0)+event.getY(1))/2 - lastTouchY

                binding.memoMv.translationX+=dx
                binding.memoMv.translationY+=dy


                lastTouchX = (event.getX(0)+event.getX(1))/2
                lastTouchY = (event.getY(0)+event.getY(1))/2
            }
        }else{
            initFlag =false
            if(binding.memoIvWriteDrawSelector.isSelected) binding.memoSvContainer.isScrollable = false
        }


        if(ev.action == MotionEvent.ACTION_DOWN){
            val x = ev.x
            val y = ev.y

            val location = IntArray(2)
            binding.memoMv.getLocationOnScreen(location)
            val rect = Rect(location[0],location[1],location[0]+binding.memoMv.width,location[1]+binding.memoMv.height)
            if(!rect.contains(x.toInt(),y.toInt())&&!binding.memoMv.modifyTextActivate){
                binding.memoMv.removeActivate()
            }
        }

        return super.dispatchTouchEvent(ev)
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

            memoFbtnOriginlocation.setOnClickListener {
                memoMv.translationX = 0f
                memoMv.translationY = 0f
            }

            memoIvGoback.apply{
                imageTintList = getColorStateList(R.color.light_gray)
                isClickable = false
            }
            memoIvGoafter.apply {
                imageTintList = getColorStateList(R.color.light_gray)
                isClickable = false
            }

            lifecycleScope.launch {
                memo.activateHistoryBtnFlow.collectLatest {
                    val (max, cur) = it
                    withContext(Dispatchers.Main){
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
                }
            }

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
                    memoSvContainer.isScrollable = false
                }else{
                    memoMv.setTextDrawMode(false)
                    memoClContainerDraw.visibility = View.GONE
                    memoClContainerWrite.visibility = View.VISIBLE
                    memoSvContainer.isScrollable = true
                }
            }
        }
    }


}