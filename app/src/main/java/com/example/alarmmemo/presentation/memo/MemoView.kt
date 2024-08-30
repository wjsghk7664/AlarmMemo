package com.example.alarmmemo.presentation.memo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.graphics.Typeface
import android.net.Uri
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import com.example.alarmmemo.R
import com.example.alarmmemo.databinding.MemoBitmapMenuBinding
import com.example.alarmmemo.databinding.MemoTextboxMenuBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MemoView(private val context: Context, attrs: AttributeSet): FrameLayout(context,attrs) {

    @Inject lateinit var colorpickerDialog: showColorpickerDialog

    var drawActivate = false

    var isPencil = true //true면 펜, false면 eraser
    var penSize = 4
    var penColor = Color.rgb(0,0,0)

    var outerFocusTitle =false
    var outerFocusTextBox =false

    private val _activateHistoryBtnFlow = MutableSharedFlow<Pair<Int,Int>>() //max, cur
    val activateHistoryBtnFlow = _activateHistoryBtnFlow.asSharedFlow()

    //텍스트 설정



    //히스토리
    private val drawHistory = ArrayDeque<HistoryItem>()
    private var curDrawHistory = -1

    private val textboxHistory = HashMap<Int,ArrayList<Triple<String,RectF,Paint>>>()
    private val bitmapHistory = HashMap<Int,ArrayList<RectF>>()

    var prevActiveText:Int? = null
    var prevActiveBitamp:Int? = null

    private var curX = 0f
    private var curY = 0f
    private var curIdx:Int? =null
    private var activatedIdx:Int?=null
    private var drawList = ArrayList<Path>()
    private var drawPaintList = ArrayList<Paint>()

    var eraserActive= false
    var addDrawActive = false

    private val textList = ArrayList<String>()
    private val textRectFList = ArrayList<RectF>()
    private val textPaintList = ArrayList<Paint>()
    private var curTextCase:Int = 0
    private var curTextIdx:Int? = null
    var modifyTextActivate = false

    private var activateTextBox:Int? = null

    private var curCase:Int = 0
    private val minHeight = dpToPx(context,10f)
    private val minWidth = dpToPx(context,10f)
    private var borderSize = dpToPx(context,8f)
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
                invalidate()
            }
            textboxMenuBold.setOnClickListener {
                activateTextBox?.let {
                    textPaintList[it].apply {
                        if(typeface.style == Typeface.BOLD){
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                            textboxMenuBold.setTextColor(context.getColor(R.color.black))
                            invalidate()
                        }else{
                            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            textboxMenuBold.setTextColor(context.getColor(R.color.orange))
                            invalidate()
                        }

                    }
                    addHistory(HistoryItem(ActionType.ModifyTextBox,activateTextBox!!,textboxHistory[activateTextBox]?.size))
                    textboxHistory[activateTextBox]!!.add(Triple(String(textList[activateTextBox!!].toCharArray()),RectF(textRectFList[activateTextBox!!]),Paint(textPaintList[activateTextBox!!])))
                }
            }
            textboxMenuColor.setOnClickListener {
                colorpickerDialog(true,context,this@MemoView)
            }
            root.visibility = View.GONE
        }
    }

    private var selStart = 0
    private var selEnd = 0
    private var zidxString = 0
    private var setterFlag = false
    val textMain by lazy {
        EditText(context).apply {
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.TRANSPARENT)
            setTypeface(typeface,Typeface.NORMAL)
            textSize = dpToPx(context,8f)
            gravity = Gravity.START
            setPadding(dpToPx(context,12f).toInt(),dpToPx(context,12f).toInt(),dpToPx(context,12f).toInt(),dpToPx(context,12f).toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setOnFocusChangeListener { v, hasFocus ->
                if(!hasFocus){
                    selEnd = -1
                }
            }

            addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    if(setterFlag) return

                    //여러문자 추가전에 히스토리 저장
                    //selection이 바뀌면 히스토리 저장
                    if(count>1||after>1||(curString.isEmpty()&&text.isEmpty())){
                        curString = deepCopyOfSpannableStringBuilder(curString)
                        addHistory(HistoryItem(ActionType.ModifyText,zidxString++,curString))
                    }else if(selEnd!=start+count){
                        curString = deepCopyOfSpannableStringBuilder(curString)
                        addHistory(HistoryItem(ActionType.ModifyText,zidxString++,curString))
                    }
                    Log.d("메모 히스토리 텍스트값","${start} / ${count} / ${after}")
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    Log.d("메모 텍스트값","${start} / ${before} / ${count}")

                    //setter로 변경되는 경우는 넘김
                    if(setterFlag) return

                    //삭제 부터 처리
                    if(before>0){
                        curString = modifyText(Pair(start,start+before),"",curString,false)
                    }
                    //텍스트 추가
                    if(count>0){
                        val addString = s?.slice(start..start+count-1)?.toString()?:""
                        curString = modifyText(Pair(start,start+1),addString,curString,true)

                        val style = StringStyle(textSize,typeface.isBold,currentTextColor)
                        curString = modifyStyle(Pair(start,start+count),style,curString)
                    }
                    selEnd=start + count
                    Log.d("메모 curstring",curString.toString())


                }

                override fun afterTextChanged(s: Editable?) {
                    Log.d("메모 텍스트 변경완료", curString.toString())
                    setterFlag = false
                }

            })



            setOnTouchListener{ _,event ->
                if(event.action == MotionEvent.ACTION_DOWN){
                    (context as MemoActivity).binding.also {
                        if(it.memoEtAddTextBox.isFocused||it.memoEtTitle.isFocused){
                            it.memoEtAddTextBox.clearFocus()
                            it.memoEtTitle.clearFocus()
                        }
                    }
                }


                if(drawActivate){
                    this@MemoView.onTouchEvent(event)
                    true
                }else{
                    false
                }
            }

            requestFocus()
        }

    }


    var isScaling = false



    private var isKeypadOn = false

    init {
        addView(bitmapMenu.root)
        addView(textboxMenu.root)
        addView(textMain)

        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = Rect()
            rootView.getWindowVisibleDisplayFrame(rect)

            val screenHeight = rootView.height
            val keypadHeight = screenHeight - rect.bottom


            val isKeyboardVisible = keypadHeight > screenHeight * 0.15

            if (isKeyboardVisible) {
                isKeypadOn=true
            } else {
                isKeypadOn =false
            }
        }
    }

    private val spanOder = HashMap<Any,Int>()
    private var spanIdx = 0

    private var curString = SpannableStringBuilder("")


    fun deepCopyOfSpannableStringBuilder(spannableStringBuilder: SpannableStringBuilder):SpannableStringBuilder{
        val newSpannable = SpannableStringBuilder(spannableStringBuilder)

        val spans = spannableStringBuilder.getSpans(0, spannableStringBuilder.length, Any::class.java)

        for (span in spans) {
            val start = spannableStringBuilder.getSpanStart(span)
            val end = spannableStringBuilder.getSpanEnd(span)
            val flags = spannableStringBuilder.getSpanFlags(span)

            newSpannable.setSpan(span, start, end, flags)
        }

        return newSpannable
    }


    fun modifyText(modifyIndex:Pair<Int,Int>,modifiedText:String, spannableText: SpannableStringBuilder,isAdd:Boolean): SpannableStringBuilder {
        val result = spannableText
        val (s,e) = modifyIndex
        Log.d("메모 수정 인덱스",modifyIndex.toString())
        return if(e-s==1&&isAdd) result.insert(s,modifiedText) else result.replace(s,e,modifiedText)
    }

    fun modifyStyle(modifyIndex: Pair<Int, Int>, modifiedStyle:StringStyle, spannableText: SpannableStringBuilder): SpannableStringBuilder {
        val result = spannableText
        val (s,e) = modifyIndex

        return result.apply {
            val abSpan = AbsoluteSizeSpan(modifiedStyle.size.toInt(),false)
            val stSpan = StyleSpan(if(modifiedStyle.isBold) Typeface.BOLD else Typeface.NORMAL)
            val foreSpan = ForegroundColorSpan(modifiedStyle.color)

            spanOder.put(abSpan,spanIdx)
            spanOder.put(stSpan,spanIdx)
            spanOder.put(foreSpan,spanIdx)
            spanIdx++

            setSpan(abSpan,s,e, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(stSpan,s,e, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(foreSpan,s,e,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

//    fun checkStyle(spannableText: SpannableStringBuilder,selectedString:Pair<Int,Int>): StringStyle?{
//        val spans =spannableText.getSpans(selectedString.first,selectedString.second ,Any::class.java)
//        var ranges = HashSet<Pair<Int,Int>>()
//        var styleSpan = ArrayList<StyleSpan>()
//        var foregroundColorSpan = ArrayList<ForegroundColorSpan>()
//        var absoluteSizeSpan = ArrayList<AbsoluteSizeSpan>()
//        for(span in spans){
//            val s = spannableText.getSpanStart(span)
//            val e = spannableText.getSpanEnd(span)
//            ranges+=Pair(s,e)
//
//            when(span){
//                is StyleSpan -> styleSpan+=span
//                is ForegroundColorSpan -> foregroundColorSpan+=span
//                is AbsoluteSizeSpan -> absoluteSizeSpan+=span
//                else -> null
//            }
//        }
//        ranges =ranges.map {
//            val newFirst=if(it.first<selectedString.first) selectedString.first else it.first
//            val newSecond=if(it.second>selectedString.second) selectedString.second else it.second
//            Pair(newFirst,newSecond)
//        }.toHashSet()
//        return if(ranges.size>1){
//            null
//        }else{
//            var lastStyle = styleSpan.getOrNull(0)
//            var max=0
//            for(i in styleSpan){
//                if(spanOder[i]!! >max){
//                    max = spanOder[i]!!
//                    lastStyle = i
//                }
//            }
//            var lastFore=foregroundColorSpan.getOrNull(0)
//            max = 0
//            for(i in foregroundColorSpan){
//                if(spanOder[i]!!>max){
//                    max = spanOder[i]!!
//                    lastFore=i
//                }
//            }
//            var lastAb = absoluteSizeSpan.getOrNull(0)
//            max = 0
//            for(i in absoluteSizeSpan){
//                if(spanOder[i]!!>max){
//                    max = spanOder[i]!!
//                    lastAb = i
//                }
//            }
//            StringStyle(
//                lastAb?.size?.toFloat()?:8f,
//                lastStyle?.let{it.style == Typeface.BOLD}?:false,
//                lastFore?.foregroundColor?:Color.BLACK
//            )
//        }
//    }


    data class StringStyle(
        val size:Float,
        val isBold:Boolean,
        val color:Int
    )

    fun setBold(isBold: Boolean){
        textMain.setTypeface(textMain.typeface,if(isBold){
            Typeface.BOLD
        }else{
            Typeface.NORMAL
        })
    }

    fun setTextSize(size:Float){
        textMain.setTextSize(size)
    }

    fun setTextColor(color:Int){
        textMain.setTextColor(color)
    }

    fun setTextDrawMode(isDrawActive:Boolean){
        removeActivate()
        drawActivate=isDrawActive
        textMain.clearFocus()
        if(!isDrawActive) {
            outerFocusTextBox = false
            outerFocusTitle = false
            textMain.requestFocus()
        }
    }

    fun removeActivate(){
        activatedIdx = null
        activateTextBox = null
        bitmapMenu.root.visibility = View.GONE
        textboxMenu.root.visibility = View.GONE
        invalidate()
    }

    private fun addHistory(historyItem: HistoryItem){
        if(curDrawHistory!=drawHistory.size-1){
            for(i in curDrawHistory+1..drawHistory.size-1){
                drawHistory.removeLast()
            }
        }
        drawHistory.addLast(historyItem)
        curDrawHistory=drawHistory.size-1

        CoroutineScope(Dispatchers.IO).launch {
            _activateHistoryBtnFlow.emit(Pair(drawHistory.size-1, curDrawHistory))
        }

    }

    fun historyGoBack(){
        textMain.clearFocus()
        textMain.post{
            if(curDrawHistory>=0){
                curDrawHistory--
                activateTextBox = null
                activatedIdx = null

                invalidate()
                CoroutineScope(Dispatchers.IO).launch {
                    _activateHistoryBtnFlow.emit(Pair(drawHistory.size-1, curDrawHistory))
                }
            }
        }


    }

    fun historyGoAfter(){
        textMain.clearFocus()
        if(curDrawHistory<drawHistory.size-1){
            curDrawHistory++
            activateTextBox = null
            activatedIdx = null
            invalidate()
            CoroutineScope(Dispatchers.IO).launch {
                _activateHistoryBtnFlow.emit(Pair(drawHistory.size-1, curDrawHistory))
            }
        }
    }

    fun addTextBox(){
        val idx = textList.size
        textList+=""
        textPaintList+=Paint().apply {
            textSize = dpToPx(context,24f)
            color = Color.BLACK
            style =Paint.Style.FILL
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        }
        textRectFList+=RectF()
        activateTextBox = idx
        modifyTextActivate = true
    }

    fun setTextBox(text:String){
        activateTextBox?.let{
            if(text.isEmpty()){
                if(textList[it]==""){
                    textList.removeAt(it)
                    textPaintList.removeAt(it)
                }else{
                    addHistory(HistoryItem(ActionType.EraseTextBox,it,textList[it]))
                }
            }else{
                if(textList[it]==""){
                    Log.d("메모 세팅",text)
                    textList[it]=text
                    initSetTextBox(it)
                    addHistory(HistoryItem(ActionType.AddTextBox,it,Triple(textList[it],textRectFList[it],textPaintList[it])))
                    textboxHistory.put(it,ArrayList())
                    textboxHistory[it]!!.add(Triple(String(textList[it].toCharArray()),RectF(textRectFList[it]),Paint(textPaintList[it])))
                }else{
                    //TODO("수정 로직 넣기")
                    textList[it]=text
                }
            }
            modifyTextActivate = false
            invalidate()
        }
        Log.d("메모 텍스트",textList.toString())
    }

    fun setActivatedTextColor(selectColor:Int){
        activateTextBox?.let {
            textPaintList[it].apply {
                color = selectColor
            }
            addHistory(HistoryItem(ActionType.ModifyTextBox,it,textboxHistory[it]?.size))
            textboxHistory[it]!!.add(Triple(String(textList[it].toCharArray()),RectF(textRectFList[it]),Paint(textPaintList[it])))
        }
        invalidate()
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
            top = dpToPx(context,80f)
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
            bitmapHistory.put(bitmaps.size-1, ArrayList())
            bitmapHistory[bitmaps.size-1]!!.add(RectF(bitmapRectFs.last()))
        }
        invalidate()
    }

    private fun addBitmap(){
        activatedIdx?.let {
            bitmaps+=Bitmap.createBitmap(bitmaps[it])
            bitmapRectFs+=RectF(bitmapRectFs[it])
            addHistory(HistoryItem(ActionType.AddBitmap,bitmaps.size-1,Pair(bitmaps.last(),bitmapRectFs.last())))
            bitmapHistory.put(bitmaps.size-1, ArrayList())
            bitmapHistory[bitmaps.size-1]!!.add(RectF(bitmapRectFs.last()))
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
            val hori = if(x in rect.left - borderSize ..rect.left+borderSize){
                1
            }else if(x in rect.left+borderSize..rect.right-borderSize){
                2
            }else if(x in rect.right-borderSize..rect.right+borderSize){
                3
            }else{
                4
            }
            val vert = if(y in rect.top - borderSize..rect.top+borderSize){
                1
            }else if(y in rect.top+borderSize..rect.bottom-borderSize){
                2
            }else if(y in rect.bottom - borderSize..rect.bottom + borderSize){
                3
            }else{
                4
            }

            Log.d("메모 케이스","${hori} / ${vert}")
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
            Log.d("메모케이스",curCase.toString())
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
        val list= generateFilteredList()
        list.forEach {
            if(it.type==MemoType.TextBox){
                val (text,rectf,paint) = it.data as Triple<String,RectF,Paint>
                if(rectf.contains(x,y)){
                    activateTextBox = it.zidx
                    activatedIdx = null

                    textPaintList[it.zidx].apply {
                        if(typeface.style == Typeface.BOLD){
                            textboxMenu.textboxMenuBold.setTextColor(context.getColor(R.color.orange))
                        }else{
                            textboxMenu.textboxMenuBold.setTextColor(context.getColor(R.color.black))
                        }
                    }
                    return
                }
            }else if(it.type==MemoType.Bitmap){
                val (bitmap,rectf) = it.data as Pair<Bitmap,RectF>
                if(rectf.contains(x,y)){
                    activatedIdx = it.zidx
                    activateTextBox = null
                    return
                }

            }
        }
        activatedIdx = null
        activateTextBox = null
    }

    private fun drawTextBitmap(canvas: Canvas, text:String, rect: RectF,paint: Paint){
        Log.d("메모텍스트",text)
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

    fun generateFilteredList(upper:Int = curDrawHistory):ArrayList<CheckItem>{
        Log.d("메모 히스토리",drawHistory.toString())
        val list = ArrayList<CheckItem>()
        var isTextExist = false
        drawHistory.filterIndexed { index, historyItem -> index<=upper }.forEach {
            when(it.action){
                ActionType.EraseTextBox -> list.remove(CheckItem(MemoType.TextBox, textboxHistory[it.zidx]!!.last(),it.zidx))
                ActionType.EraseDraw -> list.remove(CheckItem(MemoType.Draw,it.data,it.zidx))
                ActionType.EraseBitmap -> list.remove(CheckItem(MemoType.Bitmap,it.data,it.zidx))
                ActionType.AddTextBox -> list+=CheckItem(MemoType.TextBox,textboxHistory[it.zidx]!![0],it.zidx)
                ActionType.AddDraw -> list+=CheckItem(MemoType.Draw,it.data,it.zidx)
                ActionType.AddBitmap -> list+=CheckItem(MemoType.Bitmap,Pair(bitmaps[it.zidx],bitmapHistory[it.zidx]!![0]),it.zidx)
                ActionType.ModifyText -> {
                    isTextExist = true
                    curString = it.data as SpannableStringBuilder
                }
                ActionType.ModifyTextBox -> {
                    val hisIdx = it.data as Int
                    val listItem = list.filter{item -> item.zidx==it.zidx&&item.type==MemoType.TextBox }[0]
                    val listIdx = list.indexOf(listItem)
                    list[listIdx] = CheckItem(MemoType.TextBox, textboxHistory[it.zidx]!![hisIdx],it.zidx)
                }
                ActionType.ModifyBitamp -> {
                    val hisIdx = it.data as Int
                    val listItem = list.filter{item ->item.zidx == it.zidx&&item.type == MemoType.Bitmap}[0]
                    val listIdx = list.indexOf(listItem)
                    list[listIdx] = CheckItem(MemoType.Bitmap, Pair(bitmaps[it.zidx],bitmapHistory[it.zidx]!![hisIdx]),it.zidx)
                }
            }
        }
        if(!isTextExist) curString = deepCopyOfSpannableStringBuilder(curString).apply{ clear() }
        Log.d("메모 히스토리 텍스트 적용전",curString.toString())
        setterFlag = true

        val curSelection = textMain.selectionStart
        textMain.setText(curString)
        if(curSelection> textMain.text.length){
            textMain.setSelection(textMain.text.length)
        }else{
            textMain.setSelection(curSelection)
        }

        Log.d("메모 히스토리 텍스트 적용",curString.toString())

        return list
    }

    override fun dispatchDraw(canvas: Canvas) {

        val list =generateFilteredList()
        list.forEach {
            if(it.type==MemoType.Bitmap&&it.zidx == activatedIdx?:-2){
                canvas.drawBitmap(bitmaps[it.zidx],null,bitmapRectFs[it.zidx],bitmapPaint)
            }else if(it.type==MemoType.TextBox&&it.zidx==activateTextBox?:-2){
                drawTextBitmap(canvas,textList[it.zidx],textRectFList[it.zidx],textPaintList[it.zidx])
            }else{
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
        }
        Log.d("메모",list.toString())

        activatedIdx?.let {
            canvas.drawBitmap(bitmaps[it],null,bitmapRectFs[it],bitmapPaint)
            canvas.drawRect(bitmapRectFs[it],borderPaint)
        }

        activateTextBox?.let {
            drawTextBitmap(canvas,textList[it],textRectFList[it],textPaintList[it])
            canvas.drawRect(textRectFList[it],borderPaint)
        }

        if(eraserActive){
            canvas.drawCircle(curX,curY,20f,Paint().apply { style = Paint.Style.STROKE })
        }

        super.dispatchDraw(canvas)
    }


    override fun onTouchEvent(event: MotionEvent?): Boolean {

        event?.let { event ->


            if(!drawActivate){

                (context as MemoActivity).binding.memoEtTitle.clearFocus()
                textMain.requestFocus()
                val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

                if(isKeypadOn){
                    imm.hideSoftInputFromWindow(textMain.windowToken,0)
                }else{
                    imm.showSoftInput(textMain, InputMethodManager.SHOW_IMPLICIT)
                }

                return false
            }


            if(event.action==MotionEvent.ACTION_DOWN&&(outerFocusTitle||outerFocusTextBox)) return super.onTouchEvent(event)

            if(event.pointerCount >1 ||isScaling) {
                if(addDrawActive){
                    addDrawActive = false
                    drawList.removeLast()
                    drawPaintList.removeLast()
                    drawHistory.removeLast()
                    curDrawHistory--
                    CoroutineScope(Dispatchers.IO).launch {
                        _activateHistoryBtnFlow.emit(Pair(drawHistory.size-1, curDrawHistory))
                    }
                }
                eraserActive = false
                return true
            }

            val it = ModifiedMotionEvent(event.action,event.x,event.y)

            if(drawActivate){
                when(it.action){
                    MotionEvent.ACTION_DOWN -> {

                        bitmapMenu.root.visibility = View.GONE
                        textboxMenu.root.visibility = View.GONE

                        curX = it.x
                        curY = it.y

                        //포커스가 풀리는 시점엔 그림이 그려지지 않도록 하기 위한 변수
                        prevActiveText = activateTextBox
                        prevActiveBitamp = activatedIdx

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

                        if(activatedIdx==null&&activateTextBox==null&&prevActiveText==null&&prevActiveBitamp==null){
                            if(isPencil&&!isScaling){
                                addDraw(curX,curY)
                                addDrawActive= true
                            }else{
                                eraserActive=true
                            }
                            return true
                        }

                        if(curIdx!=null||curTextIdx!=null) return true
                    }
                    MotionEvent.ACTION_MOVE ->{
                        Log.d("메모",drawActivate.toString()+","+activatedIdx.toString())
                        if(activatedIdx==null&&activateTextBox==null&&prevActiveText==null&&prevActiveBitamp==null&&drawActivate){
                            if(isPencil&&!isScaling){
                                if(addDrawActive&&drawActivate){
                                    drawList.last().lineTo(it.x,it.y)
                                }
                            }else{
                                if(eraserActive){
                                    removeDraw(it.x,it.y)
                                }

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
                        if(event.pointerCount==1){
                            isScaling=false
                        }
                        activatedIdx?.let { it1 ->
                            bitmapMenu.root.visibility = View.VISIBLE
                            val x = (bitmapRectFs[it1].left+bitmapRectFs[it1].right)/2f - bitmapMenuWidth/2f
                            bitmapMenu.root.x= if(x+bitmapMenuWidth>width){
                                width - bitmapMenuWidth
                            }else if(x<0){
                                0f
                            }else{
                                x
                            }
                            val y= bitmapRectFs[it1].top - dpToPx(context,50f)
                            bitmapMenu.root.y=if(y<0) 0f else y

                            addHistory(HistoryItem(ActionType.ModifyBitamp,it1,bitmapHistory[it1]?.size))
                            bitmapHistory[it1]!!.add(RectF(bitmapRectFs[it1]))
                        }

                        activateTextBox?.let { it2 ->
                            textboxMenu.root.visibility = View.VISIBLE
                            val x =(textRectFList[it2].left+textRectFList[it2].right)/2f - textMenuWidth/2f
                            textboxMenu.root.x= if(x+textMenuWidth>width){
                                width -textMenuWidth
                            }else if(x<0){
                                0f
                            }else{
                                x
                            }
                            val y = textRectFList[it2].top - dpToPx(context,50f)
                            textboxMenu.root.y = if(y<0) 0f else y

                            addHistory(HistoryItem(ActionType.ModifyTextBox,it2,textboxHistory[it2]?.size))
                            textboxHistory[it2]!!.add(Triple(String(textList[it2].toCharArray()),RectF(textRectFList[it2]),Paint(textPaintList[it2])))
                        }
                        addDrawActive = false
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

enum class ActionType(val type:Int){
    EraseTextBox(0),
    EraseDraw(0),
    EraseBitmap(0),
    AddTextBox(1),
    AddDraw(1),
    AddBitmap(1),
    ModifyBitamp(2),
    ModifyTextBox(2),
    ModifyText(2)

}

data class HistoryItem(
    val action:ActionType,
    val zidx: Int,
    val data:Any?
)

data class CheckItem(
    val type:MemoType,
    val data:Any?,
    val zidx: Int
)

data class ModifiedMotionEvent(
    val action: Int,
    val x:Float,
    val y:Float
)

