package com.example.alarmmemo.util

import android.content.Context
import android.content.res.ColorStateList
import android.renderscript.ScriptGroup.Binding
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.viewbinding.ViewBinding
import com.example.alarmmemo.databinding.ActivityMemoBinding
import com.example.alarmmemo.databinding.DialogColorPickerBinding
import com.example.alarmmemo.presentation.memo.MemoView
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import dagger.hilt.android.qualifiers.ApplicationContext

fun showColorpickerDialog(isText:Boolean, context: Context, binding:Any){
    val cvbinding = DialogColorPickerBinding.inflate(LayoutInflater.from(context))
    val colorPickerView = cvbinding.colorpickerCv
    val alphaSlider = cvbinding.colorpickerAlphaSlideBar
    val brightnessSlideBar = cvbinding.colorpickerBrightnessSlide
    colorPickerView.attachAlphaSlider(alphaSlider)
    colorPickerView.attachBrightnessSlider(brightnessSlideBar)


    var selectedColor  = 0

    colorPickerView.setColorListener(ColorEnvelopeListener{ envelope: ColorEnvelope, fromUser: Boolean ->
        val selectColor = envelope.color


        selectedColor = selectColor
        cvbinding.colorpickerIvSelectedColor.setBackgroundColor(selectColor)
    })

    AlertDialog.Builder(context)
        .setView(cvbinding.root)
        .setPositiveButton("선택"){ dialog,_ ->
            if(binding is ActivityMemoBinding){
                if(isText){
                    binding.memoIvTextcolor.imageTintList = ColorStateList.valueOf(selectedColor)
                    binding.memoMv.textColor = selectedColor
                }else{
                    binding.memoIvPencil.imageTintList = ColorStateList.valueOf(selectedColor)
                    binding.memoMv.penColor = selectedColor
                }
            }
            if(binding is MemoView){
                binding.setActivatedTextColor(selectedColor)
            }
            dialog.dismiss()
        }.setNegativeButton("취소"){ dialog,_ ->
            dialog.dismiss()
        }.show()
}