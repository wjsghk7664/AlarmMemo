package com.example.alarmmemo.presentation.memo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet.Constraint
import androidx.core.view.drawToBitmap
import com.example.alarmmemo.R
import com.example.alarmmemo.data.model.MemoModel
import com.example.alarmmemo.databinding.MemoBitmapMenuBinding

class MemoView(private val context: Context, attrs: AttributeSet): FrameLayout(context,attrs) {
    //세팅 프로퍼티
    private var eraseOn =false
    private val history = ArrayDeque<HistoryItem>()

    private var curX = 0f
    private var curY = 0f
    private var curIdx:Int? =null
    private var activatedIdx:Int?=null
    private var drawList = ArrayList<Path>()
    private var drawPaintList = ArrayList<Paint>()
    private var drawActivate = true
    private var curCase:Int = 0
    private val minHeight = dpToPx(context,50f)
    private val minWidth = dpToPx(context,50f)
    private var borderSize = 50f
    private val bitmaps = ArrayList<Bitmap>()
    private val bitmapRectFs = ArrayList<RectF>()
    private val bitmapPaint = Paint()
    private val borderPaint = Paint().apply {
        color = context.getColor(R.color.black)
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(context,2f)
        pathEffect = DashPathEffect(floatArrayOf(10f,10f),0f)
    }
    private var drawPaint = Paint().apply {
        color = context.getColor(R.color.black)
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(context,4f)
    }

    private val bitmapMenu by lazy {
        val inflater = LayoutInflater.from(context)
        MemoBitmapMenuBinding.inflate(inflater,this@MemoView,false).apply {
            bitmapMenuDelete.setOnClickListener {
                removeBitmap()
            }
            bitmapMenuCopy.setOnClickListener{
                addBitmap()
            }
            root.visibility =View.GONE
        }
    }

    init {
        addView(bitmapMenu.root)
    }

    fun addBitmap(uri: Uri){
        getBitmapFromUri(context,uri).getOrNull()?.let {
            bitmaps+=it
            bitmapRectFs+=RectF(0f,0f, dpToPx(context,100f),it.height.toFloat()*dpToPx(context,100f)/it.width.toFloat())
            history.addLast(HistoryItem(ActionType.AddBitmap,bitmaps.size-1,Pair(bitmaps.last(),bitmapRectFs.last())))
        }
        invalidate()
    }

    private fun addBitmap(){
        activatedIdx?.let {
            bitmaps+=Bitmap.createBitmap(bitmaps[it])
            bitmapRectFs+=RectF(bitmapRectFs[it])
            history.addLast(HistoryItem(ActionType.AddBitmap,bitmaps.size-1,Pair(bitmaps.last(),bitmapRectFs.last())))
        }
        invalidate()
    }

    fun addDraw(x:Float?,y:Float?){
        if(x==null||y==null) return
        drawList+=Path().apply { moveTo(x,y) }
        drawPaintList+=Paint().apply {
            color = context.getColor(R.color.black)
            style = Paint.Style.STROKE
            strokeWidth = dpToPx(context,2f)
        }
        history.addLast(HistoryItem(ActionType.AddDraw, drawList.size-1,Pair(drawList.last(),drawPaintList.last())))
    }

    fun removeBitmap(){
        activatedIdx?.let {
            history.addLast(HistoryItem(ActionType.EraseBitmap,it,Pair(bitmaps[it],bitmapRectFs[it])))
            bitmaps.removeAt(it)
            bitmapRectFs.removeAt(it)
            activatedIdx=null
            removeView(bitmapMenu.root)
        }
        invalidate()
    }

    //1~9로 9구획으로 나눔(왼위,위,오른위,왼,내부,오른,왼아래,아래,오른아래)
    fun checkCase(x:Float,y:Float):Pair<Int,Int>?{
        bitmapRectFs.forEachIndexed { idx,rect ->
            val hori = if(x in rect.left ..rect.left+borderSize){
                1
            }else if(x in rect.left+borderSize..rect.right-borderSize){
                2
            }else if(x in rect.right-borderSize..rect.right){
                3
            }else{
                4
            }
            val vert = if(y in rect.top..rect.top+borderSize){
                1
            }else if(y in rect.top+borderSize..rect.bottom-borderSize){
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
            Log.d("메모",curCase.toString())
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

    private fun setActivate(x:Float?,y:Float?){
        if(x==null||y==null) return
        if(activatedIdx!=null&&!bitmapRectFs[activatedIdx!!].contains(x,y)){
            activatedIdx=null
        }
        bitmapRectFs.forEachIndexed { idx, rect ->
            if(rect.contains(x, y)){
                activatedIdx=idx
                return
            }
        }
        activatedIdx = null
    }

    override fun dispatchDraw(canvas: Canvas) {
        val list = ArrayList<Pair<MemoType,Any?>>()
        history.forEach {
            when(it.action){
                ActionType.EraseTextBox -> list.remove(Pair(MemoType.TextBox,it.data))
                ActionType.EraseDraw -> list.remove(Pair(MemoType.Draw,it.data))
                ActionType.EraseBitmap -> list.remove(Pair(MemoType.Bitmap,it.data))
                ActionType.EraseText -> list.remove(Pair(MemoType.Text,it.data))
                ActionType.AddTextBox -> list+=Pair(MemoType.TextBox,it.data)
                ActionType.AddDraw -> list+=Pair(MemoType.Draw,it.data)
                ActionType.AddBitmap -> list+=Pair(MemoType.Bitmap,it.data)
                ActionType.AddText -> list+=Pair(MemoType.Text,it.data)
            }
        }
        list.forEach {
            val (type,data) = it
            when(type){
                MemoType.TextBox -> null
                MemoType.Draw -> {
                    val (path,paint) = data as Pair<Path,Paint>
                    canvas.drawPath(path,paint)
                }
                MemoType.Bitmap -> {
                    val (bitmap,rectf) = data as Pair<Bitmap,RectF>
                    canvas.drawBitmap(bitmap,null,rectf,bitmapPaint)
                }
                MemoType.Text -> null
                MemoType.Default -> null
            }
        }
        Log.d("메모",list.toString())

        activatedIdx?.let {
            canvas.drawBitmap(bitmaps[it],null,bitmapRectFs[it],bitmapPaint)
            canvas.drawRect(bitmapRectFs[it],borderPaint)
        }
        super.dispatchDraw(canvas)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when(it.action){
                MotionEvent.ACTION_DOWN -> {

                    bitmapMenu.root.visibility = View.GONE

                    curX = it.x
                    curY = it.y
                    setActivate(curX,curY)
                    if(activatedIdx!=null){
                        val selected = checkCase(curX,curY)
                        if(selected == null){
                            curIdx = null
                        }else{
                            curIdx = selected.first
                            curCase = selected.second
                        }
                    }

                    invalidate()

                    if(activatedIdx==null&&drawActivate){
                        addDraw(curX,curY)
                        return true
                    }

                    if(curIdx!=null) return true
                }
                MotionEvent.ACTION_MOVE ->{
                    Log.d("메모",drawActivate.toString()+","+activatedIdx.toString())
                    if(activatedIdx==null&&drawActivate){
                        drawList.last().lineTo(it.x,it.y)
                        invalidate()
                    }
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
                        invalidate()
                    }
                    return true
                }
                MotionEvent.ACTION_UP ->{
                    if(activatedIdx!=null){
                        bitmapMenu.root.x=(bitmapRectFs[activatedIdx!!].left+bitmapRectFs[activatedIdx!!].right)/2f - bitmapMenu.root.width/2f
                        bitmapMenu.root.y=bitmapRectFs[activatedIdx!!].top - dpToPx(context,50f)
                        bitmapMenu.root.visibility = View.VISIBLE
                        bitmapMenu.root.bringToFront()
                    }
                    curIdx = null
                    invalidate()
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

enum class MemoType{
    TextBox,
    Draw,
    Bitmap,
    Text,
    Default
}

enum class ActionType{
    EraseTextBox,
    EraseDraw,
    EraseBitmap,
    EraseText,
    AddTextBox,
    AddDraw,
    AddBitmap,
    AddText

}

data class HistoryItem(
    val action:ActionType,
    val zidx: Int,
    val data:Any?
)