package com.team5.alarmmemo.presentation.memo

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ScrollView

class MemoScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
): ScrollView(context,attrs) {

    var isScrollable = true

    var scaleDetector: ScaleGestureDetector? = null


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let {
            scaleDetector?.onTouchEvent(it)
        }
        return super.dispatchTouchEvent(ev)
    }
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return super.onInterceptTouchEvent(ev)&&isScrollable
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return super.onTouchEvent(ev)&&isScrollable
    }


}