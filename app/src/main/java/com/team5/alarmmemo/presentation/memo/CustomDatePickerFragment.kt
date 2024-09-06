package com.team5.alarmmemo.presentation.memo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.team5.alarmmemo.R
import com.team5.alarmmemo.databinding.FragmentCustomDatePickerBinding

class CustomDatePickerFragment(private val year:Int, private val month:Int,private val day:Int) : DialogFragment() {

    private var _binding : FragmentCustomDatePickerBinding? = null
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
        _binding = FragmentCustomDatePickerBinding.inflate(inflater,container,false)
        return binding.root
    }

    interface OnDateSetListener{
        fun onDateSet(year: Int,month: Int,day: Int)
    }

    private var onDateSetListener:OnDateSetListener? = null

    fun setOnDateSetListener(listener:OnDateSetListener){
        onDateSetListener = listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            datepickerDatepicker.updateDate(year,month-1,day)
            datepickerNegative.setOnClickListener {
                dismiss()
            }
            datepickerPositive.setOnClickListener {
                onDateSetListener?.onDateSet(datepickerDatepicker.year,datepickerDatepicker.month+1,datepickerDatepicker.dayOfMonth)
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
        fun newInstance(year:Int, month:Int,day:Int) = CustomDatePickerFragment(year,month,day)
    }
}