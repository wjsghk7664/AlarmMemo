package com.example.alarmmemo.presentation.memo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet.Constraint
import com.example.alarmmemo.R

class MemoView(private val context: Context, attrs: AttributeSet): ConstraintLayout(context,attrs) {
    private var curX = 0f
    private var curY = 0f
    private var curIdx:Int? =null
    private var curCase:Int = 0
    private val min = 10f
    private val max = 150f
    private val minHeight = dpToPx(context,10f)
    private val minWidth = dpToPx(context,10f)
    private var borderSize = 50f
    private val bitmaps = ArrayList<Bitmap>()
    private val bitmapRectFs = ArrayList<RectF>()
    private val bitampPaint = Paint()
    private var drawPaint = Paint().apply {
        color = context.getColor(R.color.black)
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(context,4f)
    }

    fun addBitmap(uri: Uri){
        getBitmapFromUri(context,uri).getOrNull()?.let {
            bitmaps+=it
            bitmapRectFs+=RectF(0f,0f,it.width.toFloat(),it.height.toFloat())
        }
    }
    //1~9로 9구획으로 나눔(왼위,위,오른위,왼,내부,오른,왼아래,아래,오른아래)
    fun checkCase(x:Float,y:Float):Pair<Int,Int>?{
        bitmapRectFs.forEachIndexed { idx,rect ->
            val hori = if(x in rect.left..rect.left+borderSize){
                1
            }else if(x in rect.left..rect.right){
                2
            }else if(x in rect.right-borderSize..rect.right){
                3
            }else{
                4
            }
            val vert = if(y in rect.top..rect.top+borderSize){
                1
            }else if(y in rect.top..rect.bottom){
                2
            }else if(y in rect.bottom - borderSize..rect.bottom){
                3
            }else{
                4
            }

            val result = if(vert==1){
                when(hori){
                    1 -> 1
                    2 -> 2
                    3 -> 3
                    else -> 10
                }
            }else if(vert==2){
                when(hori){
                    1->4
                    2->5
                    3->6
                    else->10
                }
            }else if(vert==3){
                when(hori){
                    1->7
                    2->8
                    3->9
                    else->10
                }
            }else{
                10
            }
            if(result!=10){
                return Pair(idx,result)
            }
        }
        return null
    }

    private fun resize(offX:Float,offY:Float,x:Float?=null,y:Float?=null){
        curIdx?.let {
            if(curCase==1||curCase==2||curCase==3){
                val new = bitmapRectFs[it].top + offY
                val newHeight = bitmapRectFs[it].height() - offY
                if(newHeight in minHeight..height.toFloat()){
                    bitmapRectFs[it].top = new
                }
            }
            if(curCase==7||curCase==8||curCase==9){
                val new = bitmapRectFs[it].bottom + offY
                val newHeight = bitmapRectFs[it].height() +offY
                if(newHeight in minHeight..height.toFloat()){
                    bitmapRectFs[it].bottom = new
                }
            }
            if(curCase==1||curCase==4||curCase==7){
                val new = bitmapRectFs[it].left + offX
                val newWidth = bitmapRectFs[it].width() - offX
                if(newWidth in minWidth..width.toFloat()){
                    bitmapRectFs[it].left = new
                }
            }
            if(curCase==3||curCase==6||curCase==9){
                val new = bitmapRectFs[it].right + offX
                val newWidth = bitmapRectFs[it].width() + offX
                if(newWidth in minWidth..width.toFloat()){
                    bitmapRectFs[it].right = new
                }
            }
        }
        invalidate()
        if(x!=null&&y!=null){
            curX=x
            curY=y
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when(it.action){
                MotionEvent.ACTION_DOWN -> {
                    curX = it.x
                    curY = it.y
                    val selected = checkCase(curX,curY)
                    if(selected == null){
                        curIdx = null
                    }else{
                        curIdx = selected.first
                        curCase = selected.second
                    }
                }
                MotionEvent.ACTION_MOVE ->{
                    val offX=it.x - curX
                    val offY=it.y - curY
                    resize(offX,offY,it.x,it.y)
                    if(curCase==5){
                        curIdx?.let { idx->
                            val maxX = minOf(bitmapRectFs[idx].right+offX,width.toFloat())
                            val minX = maxOf(bitmapRectFs[idx].left+offX,0f)
                            val maxY = minOf(bitmapRectFs[idx].bottom+offY,height.toFloat())
                            val minY = maxOf(bitmapRectFs[idx].top+offY,0f)

                            val adjLeft = if(offX>0) maxX - bitmapRectFs[idx].width() else minX
                            val adjTop = if(offY>0) maxY - bitmapRectFs[idx].height() else minY

                            bitmapRectFs[idx].offsetTo(adjLeft,adjTop)
                            invalidate()
                            curX = it.x
                            curY = it.y
                        }
                    }
                    return true
                }
                MotionEvent.ACTION_UP ->{
                    return true
                }
            }
        }


        return super.onTouchEvent(event)
    }
}

fun dpToPx(context: Context, dp:Float): Float{
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        context.resources.displayMetrics
    )
}

fun getBitmapFromUri(context: Context, uri: Uri):Result<Bitmap>{
    return runCatching {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        bitmap
    }
}