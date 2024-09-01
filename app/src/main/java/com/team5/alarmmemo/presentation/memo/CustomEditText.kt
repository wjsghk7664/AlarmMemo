package com.team5.alarmmemo.presentation.memo

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import android.widget.EditText
import com.google.android.material.datepicker.OnSelectionChangedListener

class CustomEditText:androidx.appcompat.widget.AppCompatEditText {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var onSelectionChangedListener: OnSelectionChangedListener? = null

    private var latestStart = 0
    private var latestEnd =0

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        latestStart = selStart
        latestEnd = selEnd
        onSelectionChangedListener?.onSelectionChanged(selStart,selEnd)
    }

    fun setOnSelectionChangedListener(listener: OnSelectionChangedListener){
        onSelectionChangedListener = listener
    }

    fun callOnSelection() = onSelectionChangedListener?.onSelectionChanged(latestStart,latestEnd)

    interface OnSelectionChangedListener{
        fun onSelectionChanged(selStart: Int, selEnd: Int)
    }

}