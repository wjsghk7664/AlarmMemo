package com.example.alarmmemo.presentation.memo

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.text.TextUtils
import android.text.method.KeyListener
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListPopupWindow
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.alarmmemo.R
import com.example.alarmmemo.databinding.ActivityMemoBinding
import com.example.alarmmemo.databinding.DialogColorPickerBinding
import com.example.alarmmemo.util.showColorpickerDialog
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MemoActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMemoBinding.inflate(layoutInflater)
    }


    private val fontList = List(61){it+4}
    private val pencilList = List(10){it+1}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initView()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

        if(ev.action == MotionEvent.ACTION_DOWN){
            val x = ev.x
            val y = ev.y

            val location = IntArray(2)
            binding.memoMv.getLocationOnScreen(location)
            val rect = Rect(location[0],location[1],location[0]+binding.memoMv.width,location[1]+binding.memoMv.height)
            if(!rect.contains(x.toInt(),y.toInt())){
                binding.memoMv.removeActivate()
            }
        }


        return super.dispatchTouchEvent(ev)
    }


    fun initView() = with(binding){

        root.setOnTouchListener{ view, motionEvent ->
            if(motionEvent.action == MotionEvent.ACTION_DOWN){
                val curView = currentFocus
                if(currentFocus is EditText){
                    curView?.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(curView?.windowToken,0)
                }



            }
            false
        }

        val photoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()){uri ->
            uri?.let {
                memoMv.addBitmap(uri)
            }
        }

        memoIvAddBitmap.setOnClickListener {
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }



        val inputType = memoEtTitle.keyListener
        memoEtTitle.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(!hasFocus){
                memoEtTitle.ellipsize = TextUtils.TruncateAt.END
                memoEtTitle.keyListener=null
                memoMv.outerFocusTitle = false
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
                memoMv.outerFocusTextBox = false
                memoEtAddTextBox.keyListener = null
                memoMv.setTextBox(memoEtAddTextBox.text.toString())
                Log.d("메모 텍스트 내용",memoEtAddTextBox.text.toString())
                memoEtAddTextBox.setText("")
                memoEtAddTextBox.visibility = View.GONE
            }else{
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
            setSelection(8)
            onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    memoMv.textSize = fontList[position]
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
                        memo.bold = true
                    }else{
                        imageTintList = getColorStateList(R.color.black)
                        memo.bold = false
                    }
                }
            }

            memoFlTextcolorContainer.setOnClickListener {
                showColorpickerDialog(true,this@MemoActivity,binding)
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
                        showColorpickerDialog(false,this@MemoActivity,binding)
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
                    memoMv.drawActivate=true
                    memoClContainerDraw.visibility = View.VISIBLE
                    memoClContainerWrite.visibility = View.GONE
                }else{
                    memoMv.drawActivate=false
                    memoClContainerDraw.visibility = View.GONE
                    memoClContainerWrite.visibility = View.VISIBLE
                }
            }
        }
    }


}