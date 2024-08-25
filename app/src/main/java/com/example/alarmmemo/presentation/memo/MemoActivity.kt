package com.example.alarmmemo.presentation.memo

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
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
import com.example.alarmmemo.R
import com.example.alarmmemo.databinding.ActivityMemoBinding
import com.example.alarmmemo.databinding.DialogColorPickerBinding
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener


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
                showColorpickerDialog(true)
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
                        showColorpickerDialog(false)
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

    private fun showColorpickerDialog(isText:Boolean){
        val cvbinding = DialogColorPickerBinding.inflate(layoutInflater)
        val colorPickerView = cvbinding.colorpickerCv
        val alphaSlider = cvbinding.colorpickerAlphaSlideBar
        val brightnessSlideBar = cvbinding.colorpickerBrightnessSlide
        colorPickerView.attachAlphaSlider(alphaSlider)
        colorPickerView.attachBrightnessSlider(brightnessSlideBar)

        var selectedColor = if(isText) binding.memoMv.textColor else binding.memoMv.penColor
        cvbinding.colorpickerIvSelectedColor.setBackgroundColor(selectedColor)

        colorPickerView.setColorListener(ColorEnvelopeListener{ envelope: ColorEnvelope, fromUser: Boolean ->
            val selectColor = envelope.color


            selectedColor = selectColor
            cvbinding.colorpickerIvSelectedColor.setBackgroundColor(selectColor)
        })

        AlertDialog.Builder(this)
            .setView(cvbinding.root)
            .setPositiveButton("선택"){ dialog,_ ->
                if(isText){
                    binding.memoIvTextcolor.imageTintList = ColorStateList.valueOf(selectedColor)
                    binding.memoMv.textColor = selectedColor
                }else{
                    binding.memoIvPencil.imageTintList = ColorStateList.valueOf(selectedColor)
                    binding.memoMv.penColor = selectedColor
                }
                dialog.dismiss()
            }.setNegativeButton("취소"){ dialog,_ ->
                dialog.dismiss()
            }.show()
    }
}