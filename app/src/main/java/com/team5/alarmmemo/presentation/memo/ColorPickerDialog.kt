package com.team5.alarmmemo.presentation.memo

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.graphics.drawable.shapes.RoundRectShape
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.viewbinding.ViewBinding
import com.team5.alarmmemo.databinding.ActivityMemoBinding
import com.team5.alarmmemo.databinding.DialogColorPickerBinding
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class showColorpickerDialog @Inject constructor(){

    val savedColors = ArrayDeque<Int>().apply {
        addFirst(Color.BLACK)
    }

    private fun addColor(color:Int){
        if(!savedColors.contains(color)){
            savedColors.addFirst(color)
        }

        if(savedColors.size>9){
            savedColors.removeLast()
        }
    }

    private fun setColors(binding:DialogColorPickerBinding){
        val saveColorList = listOf(
            binding.colorpickerIvSavedColor1,
            binding.colorpickerIvSavedColor2,
            binding.colorpickerIvSavedColor3,
            binding.colorpickerIvSavedColor4,
            binding.colorpickerIvSavedColor5,
            binding.colorpickerIvSavedColor6,
            binding.colorpickerIvSavedColor7,
            binding.colorpickerIvSavedColor8,
            binding.colorpickerIvSavedColor9
        )


        saveColorList.forEachIndexed { idx, iv ->
            val circleDrawable = ShapeDrawable(OvalShape()).apply {
                paint.color = savedColors.getOrNull(idx)?:Color.TRANSPARENT
                paint.style = Paint.Style.FILL
            }
            iv.setBackground(circleDrawable)
            iv.setOnClickListener {
                savedColors.getOrNull(idx)?.let {
                    binding.colorpickerCv.setInitialColor(it)
                }

            }
        }
    }

    operator fun invoke(isText:Boolean, context: Context, binding:Any){
        val cvbinding = DialogColorPickerBinding.inflate(LayoutInflater.from(context))
        val colorPickerView = cvbinding.colorpickerCv
        val alphaSlider = cvbinding.colorpickerAlphaSlideBar
        val brightnessSlideBar = cvbinding.colorpickerBrightnessSlide
        colorPickerView.attachAlphaSlider(alphaSlider)
        colorPickerView.attachBrightnessSlider(brightnessSlideBar)

        setColors(cvbinding)


        var selectedColor  = 0

        colorPickerView.setColorListener(ColorEnvelopeListener{ envelope: ColorEnvelope, fromUser: Boolean ->
            val selectColor = envelope.color


            selectedColor = selectColor
            val circleDrawable = ShapeDrawable(OvalShape()).apply {
                paint.color = selectColor
                paint.style = Paint.Style.FILL
            }
            cvbinding.colorpickerIvSelectedColor.setBackground(circleDrawable)
        })

        AlertDialog.Builder(context)
            .setView(cvbinding.root)
            .setPositiveButton("선택"){ dialog,_ ->
                if(binding is ActivityMemoBinding){
                    if(isText){
                        binding.memoIvTextcolor.imageTintList = ColorStateList.valueOf(selectedColor)
                        binding.memoMv.setTextColor(selectedColor)
                    }else{
                        binding.memoIvPencil.imageTintList = ColorStateList.valueOf(selectedColor)
                        binding.memoMv.penColor = selectedColor
                    }
                }
                if(binding is MemoView){
                    binding.setActivatedTextColor(selectedColor)
                }
                addColor(selectedColor)
                dialog.dismiss()
            }.setNegativeButton("취소"){ dialog,_ ->
                dialog.dismiss()
            }.show()
    }

}