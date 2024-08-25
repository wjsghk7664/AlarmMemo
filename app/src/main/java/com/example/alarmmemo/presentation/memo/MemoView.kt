package com.example.alarmmemo.presentation.memo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.graphics.Typeface
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet.Constraint
import androidx.core.view.drawToBitmap
import com.example.alarmmemo.R
import com.example.alarmmemo.data.model.MemoModel
import com.example.alarmmemo.databinding.MemoBitmapMenuBinding
import com.example.alarmmemo.databinding.MemoTextboxMenuBinding

class MemoView(private val context: Context, attrs: AttributeSet): FrameLayout(context,attrs) {
    //세팅 프로퍼티

    var drawActivate = false

    var bold = false
    var textSize = 12
    var textColor = Color.rgb(0,0,0)

    var isPencil = true //true면 펜, false면 eraser
    var penSize = 4
    var penColor = Color.rgb(0,0,0)

    var outerFocusTitle =false
    var outerFocusTextBox =false


    //히스토리
    private val drawHistory = ArrayDeque<HistoryItem>()
    private var curDrawHistory = -1

    private var curX = 0f
    private var curY = 0f
    private var curIdx:Int? =null
    private var activatedIdx:Int?=null
    private var drawList = ArrayList<Path>()
    private var drawPaintList = ArrayList<Paint>()

    private var eraserActive= false

    private val textList = ArrayList<String>()
    private val textRectFList = ArrayList<RectF>()
    private val textPaintList = ArrayList<Paint>()
    private var curTextCase:Int = 0
    private var curTextIdx:Int? = null

    private var activateTextBox:Int? = null

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

    private val bitmapMenuWidth = dpToPx(context,196f)
    private val textMenuWidth = dpToPx(context,264f)

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

    private val textboxMenu by lazy {
        val inflater = LayoutInflater.from(context)
        MemoTextboxMenuBinding.inflate(inflater,this@MemoView, false).apply {
            textboxMenuDelete.setOnClickListener{
                removeTextBox()
            }
            textboxMenuBold.setOnClickListener {
                activateTextBox?.let {
                    textPaintList[it].apply {
                        if(typeface.style == Typeface.BOLD){
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                            textboxMenuBold.setTextColor(context.getColor(R.color.black))
                        }else{
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            textboxMenuBold.setTextColor(context.getColor(R.color.orange))
                        }

                    }
                }
            }
            root.visibility = View.GONE
        }
    }

    init {
        addView(bitmapMenu.root)
        addView(textboxMenu.root)
    }

    fun addHistory(historyItem: HistoryItem){
        if(curDrawHistory!=drawHistory.size-1){
            for(i in curDrawHistory+1..drawHistory.size-1){
                drawHistory.removeLast()
            }
        }
        drawHistory.addLast(historyItem)
        curDrawHistory=drawHistory.size-1
        Log.d("메모 cur2",curDrawHistory.toString())
    }

    fun addTextBox(){
        val idx = textList.size
        textList+=""
        textPaintList+=Paint().apply {
            textSize = dpToPx(context,24f)
            color = Color.BLACK
            style =Paint.Style.FILL

        }
        textRectFList+=RectF()
        activateTextBox = idx
    }

    fun setTextBox(text:String){
        activateTextBox?.let{
            if(text.isEmpty()){
                if(textList[it]==""){
                    textList.removeAt(it)
                }else{
                    addHistory(HistoryItem(ActionType.EraseTextBox,it,textList[it]))
                }
            }else{
                if(textList[it]==""){
                    textList[it]=text
                    initSetTextBox(it)
                    addHistory(HistoryItem(ActionType.AddTextBox,it,Triple(textList[it],textRectFList[it],textPaintList[it])))
                }else{
                    addHistory(HistoryItem(ActionType.ModifyTextBox,it,Pair(textList[it],text))) //수정전 - 수정후
                    textList[it]=text
                }
            }
        }
        Log.d("메모 텍스트",textList.toString())
    }

    private fun initSetTextBox(idx:Int){
        val paint = textPaintList[idx]

        val textLines = textList[idx].split("\n")
        var maxWidth = 0f
        var totalHeight = 0f

        for (line in textLines) {
            val lineWidth = paint.measureText(line)
            maxWidth = maxOf(maxWidth, lineWidth)
            val fontMetrics = paint.fontMetrics
            totalHeight += (fontMetrics.descent - fontMetrics.ascent)
        }

        textRectFList[idx] = RectF().apply {
            left=(width - maxWidth)/2f
            top = dpToPx(context,20f)
            right = left + maxWidth
            bottom = top +totalHeight
        }
        invalidate()
    }

    fun removeTextBox(){
        activateTextBox?.let {
            addHistory(HistoryItem(ActionType.EraseTextBox,it,Triple(textList[it],textRectFList[it],textPaintList[it])))
            activateTextBox = null
            textboxMenu.root.visibility = View.GONE
        }
    }

    fun addBitmap(uri: Uri){
        getBitmapFromUri(context,uri).getOrNull()?.let {
            bitmaps+=it
            bitmapRectFs+=RectF((width- dpToPx(context,100f))/2f,30f, (width- dpToPx(context,100f))/2f+dpToPx(context,100f),30f+it.height.toFloat()*dpToPx(context,100f)/it.width.toFloat())
            addHistory(HistoryItem(ActionType.AddBitmap,bitmaps.size-1,Pair(bitmaps.last(),bitmapRectFs.last())))
        }
        invalidate()
    }

    private fun addBitmap(){
        activatedIdx?.let {
            bitmaps+=Bitmap.createBitmap(bitmaps[it])
            bitmapRectFs+=RectF(bitmapRectFs[it])
            addHistory(HistoryItem(ActionType.AddBitmap,bitmaps.size-1,Pair(bitmaps.last(),bitmapRectFs.last())))
        }
        invalidate()
    }

    fun removeBitmap(){
        activatedIdx?.let {
            addHistory(HistoryItem(ActionType.EraseBitmap,it,Pair(bitmaps[it],bitmapRectFs[it])))
            activatedIdx=null
            bitmapMenu.root.visibility=View.GONE
        }
        invalidate()
    }

    fun addDraw(x:Float?,y:Float?){
        if(x==null||y==null) return
        drawList+=Path().apply { moveTo(x,y) }
        drawPaintList+=Paint().apply {
            color = penColor
            style = Paint.Style.STROKE
            strokeWidth = dpToPx(context,penSize.toFloat())
        }
        addHistory(HistoryItem(ActionType.AddDraw, drawList.size-1,Pair(drawList.last(),drawPaintList.last())))
    }

    fun removeDraw(x:Float?,y:Float?){
        if(x==null||y==null) return
        drawList.forEachIndexed { idx, it ->
            val region = Region().apply {
                setPath(it, Region(0,0,width,height))
            }
            if(region.contains(x.toInt(),y.toInt())){
                addHistory(HistoryItem(ActionType.EraseDraw, idx, Pair(drawList[idx],drawPaintList[idx])))
            }
        }
        invalidate()
    }

    //1~9로 9구획으로 나눔(왼위,위,오른위,왼,내부,오른,왼아래,아래,오른아래)
    fun checkCase(x:Float,y:Float, bitmapRectFs:ArrayList<RectF>):Pair<Int,Int>?{
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

    private fun resize(offX:Float,offY:Float,x:Float?=null,y:Float?=null, curIdx:Int?, curCase:Int,bitmapRectFs: ArrayList<RectF>){
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

    private fun moveRectF(x:Float?, y:Float?, offX: Float, offY: Float, bitmapRectFs: ArrayList<RectF>, curIdx: Int?){
        if(x==null||y==null) return
        curIdx?.let { idx->
            val maxX = minOf(bitmapRectFs[idx].right+offX,width.toFloat())
            val minX = maxOf(bitmapRectFs[idx].left+offX,0f)
            val maxY = minOf(bitmapRectFs[idx].bottom+offY,height.toFloat())
            val minY = maxOf(bitmapRectFs[idx].top+offY,0f)

            val adjLeft = if(offX>0) maxX - bitmapRectFs[idx].width() else minX
            val adjTop = if(offY>0) maxY - bitmapRectFs[idx].height() else minY

            bitmapRectFs[idx].offsetTo(adjLeft,adjTop)
            invalidate()
            curX = x
            curY = y
        }
        invalidate()
    }

    private fun setActivate(x:Float?,y:Float?){
        if(x==null||y==null) return
        if(activateTextBox!=null&&!textRectFList[activateTextBox!!].contains(x,y)){
            activatedIdx=null
        }
        if(activatedIdx!=null&&!bitmapRectFs[activatedIdx!!].contains(x,y)){
            activatedIdx=null
        }

        textRectFList.forEachIndexed { idx, rect ->
            if(rect.contains(x,y)){
                activateTextBox = idx
                activatedIdx=null
                return
            }
        }

        bitmapRectFs.forEachIndexed { idx, rect ->
            if(rect.contains(x, y)){
                activatedIdx=idx
                activateTextBox = null
                return
            }
        }
        activatedIdx = null
        activateTextBox = null
    }

    private fun drawTextBitmap(canvas: Canvas, text:String, rect: RectF,paint: Paint){
        val textBounds = Rect()
        val lines = text.split("\n")
        var maxWidth = 0
        var totalHeight = 0


        lines.forEach { line ->
            paint.getTextBounds(line, 0, line.length, textBounds)
            maxWidth = maxOf(maxWidth, textBounds.width())
            totalHeight += textBounds.height()
        }


        val bitmap = Bitmap.createBitmap(maxWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val tempCanvas = Canvas(bitmap)


        var yOffset = 0f
        lines.forEach { line ->
            paint.getTextBounds(line, 0, line.length, textBounds)
            tempCanvas.drawText(line, -textBounds.left.toFloat(), yOffset - textBounds.top.toFloat(), paint)
            yOffset += textBounds.height()
        }

        val srcRect = Rect(0, 0, maxWidth, totalHeight)
        canvas.drawBitmap(bitmap, srcRect, rect, null)

        bitmap.recycle()

    }

    override fun dispatchDraw(canvas: Canvas) {
        val list = ArrayList<CheckItem>()
        Log.d("메모 히스토리",drawHistory.toString())
        Log.d("메모 cur",curDrawHistory.toString())
        drawHistory.filterIndexed { index, historyItem -> index<=curDrawHistory }.forEach {
            Log.d("메모 타입",it.data.hashCode().toString()+"/"+it.data.toString())
            when(it.action){
                ActionType.EraseTextBox -> list.remove(CheckItem(MemoType.TextBox,it.data))
                ActionType.EraseDraw -> list.remove(CheckItem(MemoType.Draw,it.data))
                ActionType.EraseBitmap -> list.remove(CheckItem(MemoType.Bitmap,it.data))
                ActionType.EraseText -> list.remove(CheckItem(MemoType.Text,it.data))
                ActionType.AddTextBox -> list+=CheckItem(MemoType.TextBox,it.data)
                ActionType.AddDraw -> list+=CheckItem(MemoType.Draw,it.data)
                ActionType.AddBitmap -> list+=CheckItem(MemoType.Bitmap,it.data)
                ActionType.AddText -> list+=CheckItem(MemoType.Text,it.data)
                ActionType.ModifyTextBox -> null//TODO
            }
        }
        Log.d("메모 리스트 개수",list.size.toString())
        list.forEach {
            val (type,data) = Pair(it.type,it.data)
            when(type){
                MemoType.TextBox -> {
                    val (text,rectf,paint) = data as Triple<String,RectF,Paint>
                    drawTextBitmap(canvas,text, rectf, paint)
                }
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

        activateTextBox?.let {
            val (text,rectf,paint) = Triple(textList[it],textRectFList[it],textPaintList[it])
            drawTextBitmap(canvas,text, rectf, paint)
            canvas.drawRect(textRectFList[it],borderPaint)
        }

        if(eraserActive){
            canvas.drawCircle(curX,curY,20f,Paint().apply { style = Paint.Style.STROKE })
        }

        super.dispatchDraw(canvas)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            if(it.action==MotionEvent.ACTION_DOWN&&(outerFocusTitle||outerFocusTextBox)) return super.onTouchEvent(event)

            if(!drawActivate){

            }else{
                when(it.action){
                    MotionEvent.ACTION_DOWN -> {

                        bitmapMenu.root.visibility = View.GONE
                        textboxMenu.root.visibility = View.GONE

                        curX = it.x
                        curY = it.y
                        setActivate(curX,curY)

                        if(activateTextBox!=null){
                            val selected = checkCase(curX,curY,textRectFList)
                            if(selected == null){
                                curTextIdx = null
                            }else{
                                curTextIdx = selected.first
                                curTextCase = selected.second
                            }
                        }

                        if(activatedIdx!=null){
                            val selected = checkCase(curX,curY,bitmapRectFs)
                            if(selected == null){
                                curIdx = null
                            }else{
                                curIdx = selected.first
                                curCase = selected.second
                            }
                        }

                        invalidate()

                        if(activatedIdx==null&&activateTextBox==null){
                            if(isPencil){
                                addDraw(curX,curY)
                            }else{
                                eraserActive=true
                            }
                            return true
                        }

                        if(curIdx!=null||curTextIdx!=null) return true
                    }
                    MotionEvent.ACTION_MOVE ->{
                        Log.d("메모",drawActivate.toString()+","+activatedIdx.toString())
                        if(activatedIdx==null&&activateTextBox==null&&drawActivate){
                            if(isPencil){
                                drawList.last().lineTo(it.x,it.y)

                            }else{
                                removeDraw(curX,curY)
                            }
                            invalidate()
                        }
                        val offX=it.x - curX
                        val offY=it.y - curY
                        resize(offX,offY,it.x,it.y,curIdx, curCase, bitmapRectFs)
                        resize(offX,offY,it.x,it.y,curTextIdx,curTextCase,textRectFList)
                        if(curCase==5){
                            moveRectF(it.x,it.y,offX,offY,bitmapRectFs,curIdx)
                        }
                        if(curTextCase==5){
                            moveRectF(it.x,it.y,offX,offY,textRectFList,curTextIdx)
                        }
                        return true
                    }
                    MotionEvent.ACTION_UP ->{
                        if(activatedIdx!=null){
                            bitmapMenu.root.visibility = View.VISIBLE
                            val x = (bitmapRectFs[activatedIdx!!].left+bitmapRectFs[activatedIdx!!].right)/2f - bitmapMenuWidth/2f
                            bitmapMenu.root.x= if(x+bitmapMenuWidth>width){
                                width - bitmapMenuWidth
                            }else if(x<0){
                                0f
                            }else{
                                x
                            }
                            val y= bitmapRectFs[activatedIdx!!].top - dpToPx(context,50f)
                            bitmapMenu.root.y=if(y<0) 0f else y
                        }
                        if(activateTextBox!=null){
                            textboxMenu.root.visibility = View.VISIBLE
                            val x =(textRectFList[activateTextBox!!].left+textRectFList[activateTextBox!!].right)/2f - textMenuWidth/2f
                            textboxMenu.root.x= if(x+textMenuWidth>width){
                                width -textMenuWidth
                            }else if(x<0){
                                0f
                            }else{
                                x
                            }
                            val y = textRectFList[activateTextBox!!].top - dpToPx(context,50f)
                            textboxMenu.root.y = if(y<0) 0f else y
                        }
                        eraserActive=false
                        curIdx = null
                        curTextIdx = null
                        invalidate()
                        return true
                    }
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
    AddText,
    ModifyTextBox

}

data class HistoryItem(
    val action:ActionType,
    val zidx: Int,
    val data:Any?
)

data class CheckItem(
    val type:MemoType,
    val data:Any?
){

}

