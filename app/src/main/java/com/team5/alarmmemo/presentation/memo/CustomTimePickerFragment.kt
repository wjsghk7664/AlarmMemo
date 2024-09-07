package com.team5.alarmmemo.presentation.memo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.team5.alarmmemo.R
import com.team5.alarmmemo.databinding.FragmentCustomTimePickerBinding


class CustomTimePickerFragment(private val hour:Int,private val minute:Int) : DialogFragment() {

    private var _binding : FragmentCustomTimePickerBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = true
        setStyle(STYLE_NORMAL,R.style.dialog_transparentDim)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCustomTimePickerBinding.inflate(inflater,container,false)
        return binding.root
    }

    interface OnTimeSetListener{
        fun onTimeSet(hour:Int, min:Int)
    }

    private var timeListener: OnTimeSetListener? = null

    fun setOnTimeSetListener(listener: OnTimeSetListener){
        timeListener = listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            timepickerTimepicker.setIs24HourView(true)
            timepickerTimepicker.hour = this@CustomTimePickerFragment.hour
            timepickerTimepicker.minute = this@CustomTimePickerFragment.minute

            timepickerNegative.setOnClickListener {
                dismiss()
            }
            timepickerPositive.setOnClickListener {
                timeListener?.onTimeSet(timepickerTimepicker.hour,timepickerTimepicker.minute)
                dismiss()
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        @JvmStatic
        fun newInstance(hour:Int,minute:Int) = CustomTimePickerFragment(hour,minute)
    }
}