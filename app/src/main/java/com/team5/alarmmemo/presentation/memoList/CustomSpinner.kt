package com.team5.alarmmemo.presentation.memoList

import android.content.Context
import android.util.AttributeSet
import android.widget.ArrayAdapter
import android.widget.ListPopupWindow
import android.widget.Spinner
import android.widget.SpinnerAdapter

class CustomSpinner : Spinner {

    private val dropdownHeight = 500 // 원하는 드롭다운 높이 (픽셀 단위)

    constructor(context: Context) : super(context)
    constructor(context: Context, mode: Int) : super(context, mode)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun setAdapter(adapter: SpinnerAdapter?) {
        super.setAdapter(adapter)
        val popup = getPopup() as? ListPopupWindow
        popup?.height = dropdownHeight
    }

    private fun getPopup(): Any? {
        val field = Spinner::class.java.getDeclaredField("mPopup")
        field.isAccessible = true
        return field.get(this)
    }
}