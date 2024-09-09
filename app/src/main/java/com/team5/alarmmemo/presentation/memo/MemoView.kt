package com.team5.alarmmemo.presentation.memo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import com.team5.alarmmemo.R
import com.team5.alarmmemo.databinding.MemoBitmapMenuBinding
import com.team5.alarmmemo.databinding.MemoTextboxMenuBinding
import com.team5.alarmmemo.util.DpPxUtil.dpToPx
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MemoView(private val context: Context, attrs: AttributeSet): FrameLayout(context,attrs) {

    @Inject lateinit var colorpickerDialog: showColorpickerDialog

    interface OnMemoChangeListener{
        suspend fun onMemoChange(str:SpannableStringBuilder?)
    }
    private var onMemoChangeListener : OnMemoChangeListener? = null
    fun setOnMemoChangeListener(listener: OnMemoChangeListener){
        onMemoChangeListener = listener
    }

    interface OnDrawChangeListener{
        suspend fun onDrawChange(draw:List<CheckItem>)
    }
    private var onDrawChangeListener : OnDrawChangeListener? = null
    fun setOnDrawChangeListener(listener: OnDrawChangeListener){
        onDrawChangeListener = listener
    }


    var latestList:List<CheckItem> = listOf()


    var fixedDrawList : MutableList<CheckItem> = mutableListOf()
    var fixedString = SpannableStringBuilder("")

    private var isSettingAutoChagned = false


    var drawActivate = false

    var isPencil = true //true면 펜, false면 eraser
    var penSize = 4
    var penColor = Color.rgb(0,0,0)

    var outerFocusTitle =false
    var outerFocusTextBox =false

    private var onActivateHistoryBtnListener: OnActivateHistoryBtnListener? = null

    private var onStyleButtonNotifyListener: OnStyleButtonNotifyListener? = null

    private var modifyTextBoxListener:OnModifyTextBoxListener? = null

    interface OnModifyTextBoxListener{
        fun onModifyTextBox()
    }

    interface OnActivateHistoryBtnListener{
        fun onActivateHistoryBtn(max:Int, cur:Int)
    }

    interface OnStyleButtonNotifyListener{
        fun onStyleButtonNotify(style:StringStyle)
    }

    fun setOnModifyTextBoxListener(listener: OnModifyTextBoxListener){
        modifyTextBoxListener = listener
    }

    fun setOnActivateHistoryBtnListener(listener: OnActivateHistoryBtnListener){
        onActivateHistoryBtnListener = listener
    }

    fun setOnStyleButtonNotifyListener(listener: OnStyleButtonNotifyListener){
        onStyleButtonNotifyListener = listener
    }

    //히스토리
    private val drawHistory = ArrayDeque<HistoryItem>()
    @Volatile
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

    val textList = ArrayList<String>()
    private val textRectFList = ArrayList<RectF>()
    private val textPaintList = ArrayList<Paint>()
    private var curTextCase:Int = 0
    private var curTextIdx:Int? = null
    var modifyTextActivate = false

    var activateTextBox:Int? = null

    private var curCase:Int = 0
    private val minHeight = dpToPx(10f)
    private val minWidth = dpToPx(10f)
    private var borderSize = dpToPx(8f)
    private val bitmaps = ArrayList<Bitmap>()
    private val bitmapRectFs = ArrayList<RectF>()
    private val bitmapPaint = Paint()
    private val borderPaint = Paint().apply {
        color = context.getColor(R.color.black)
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(2f)
        pathEffect = DashPathEffect(floatArrayOf(10f,10f),0f)
    }
    private var drawPaint = Paint().apply {
        color = context.getColor(R.color.black)
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(4f)
    }







    private val bitmapMenuWidth = dpToPx(196f)
    private val textMenuWidth = dpToPx(364f)

    val bitmapMenu by lazy {
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

    val textboxMenu by lazy {
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
            textboxMenuModify.setOnClickListener {
                modifyTextBoxListener?.onModifyTextBox()
                root.visibility = View.GONE
            }
            root.visibility = View.GONE
        }
    }

    private var textSize = 24f
    private var isBold = false
    private var textColor = Color.BLACK


    private var isSettingChanged = false
    private var changedFocus = false

    private var historyMove = false

    private var textCheck:String? = ""

    private var zidxText = 0

    private var lastTextHistoryItem:HistoryItem? =null
    private var secondLastTextHistoryItem: HistoryItem? = null

    private var isUnitSettingChanged = false

    private var initText = true


    private var changedCurEditable = false

    private var selectionChangeTest = 0

    private var prevStart = 0

    private var prevText = ""

    val textMain by lazy {
        CustomEditText(context).apply {
            setText("")
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.TRANSPARENT)
            setTypeface(typeface,Typeface.NORMAL)
            textSize = 24f
            gravity = Gravity.START
            setPadding(
                dpToPx(12f).toInt(),
                dpToPx(12f).toInt(),
                dpToPx(12f).toInt(),
                dpToPx(12f).toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            setOnFocusChangeListener { v, hasFocus ->
                if(!hasFocus){
                    changedFocus = true
                    Log.d("메모 셀렉션 포커스","포커스 잃음")
                }
            }


            setOnSelectionChangedListener(object :CustomEditText.OnSelectionChangedListener{
                override fun onStartSelectionChange(prevStart: Int, prevEnd: Int) {

                }

                override fun onSelectionChanged(selStart: Int, selEnd: Int) {
                    Log.d("셀렉션 변경 테스트","${selectionChangeTest++}:${selStart}/${selEnd}")
                    Log.d("히스토리",drawHistory.toString())

                    if(selEnd==selStart&&text!=null&&prevText==text.toString()){
                        val spans = text!!.getSpans(selEnd,selEnd, Any::class.java).filter{ text!!.getSpanStart(it)!=selEnd}
                        var style:Boolean? =null
                        var size:Int? =null
                        var color:Int? =null
                        for(i in spans){
                            when(i){
                                is StyleSpan -> style = i.style == Typeface.BOLD
                                is ForegroundColorSpan -> color = i.foregroundColor
                                is AbsoluteSizeSpan -> {
                                    size = i.size
                                    Log.d("텍스트 사이즈",size.toString())
                                    Log.d("변환값",context.resources.displayMetrics.density.toString())
                                }
                            }
                        }
                        val stringStyle=StringStyle(size?.toFloat()?: dpToPx(24f),style?:false,color?:Color.BLACK)
                        textColor = stringStyle.color
                        isBold = stringStyle.isBold
                        isSettingAutoChagned = true
                        onStyleButtonNotifyListener?.onStyleButtonNotify(stringStyle)
                    }

                    prevText=text?.toString()?:""
                    Log.d("변경점","${textCheck} / ${text.toString()} / ${selStart} / ${selEnd}")



                }

            })

            addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    if((start+count!=prevStart||selectionStart!=selectionEnd)&&!historyMove){
                        Log.d("히스토리 start","${start},${after},${count},${prevStart},${selectionStart},${selectionEnd}")
                        saveHistoryOfEditable(lastTextHistoryItem?.data as SpannableStringBuilder?)

                    }
                    historyMove = false
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    Log.d("텍스트 변경 테스트",s.toString())

                    prevStart = start + count

                }

                override fun afterTextChanged(s: Editable?) {
                    if(isUnitSettingChanged){
                        Log.d("히스토리 유닛 세팅","${prevStart}")
                        modifyRangeText(prevStart -1, prevStart,s)
                        isUnitSettingChanged = false
                        post {
                            setSelection(prevStart,prevStart)
                        }

                    }


                    if(initText){
                        Log.d("히스토리 추가","3")
                        saveHistoryOfEditable(s)
                        initText = false
                    }
                    lastTextHistoryItem?.data = deepCopyOfSpannableStringBuilder(s as SpannableStringBuilder)

                    CoroutineScope(Dispatchers.IO).launch{
                        (lastTextHistoryItem?.data as SpannableStringBuilder?)?.let { spans ->
                            spans.getSpans(0,spans.length,Any::class.java).forEach {
                                Log.d("저장시점 스팬 체크",it.toString()+": ${spans.getSpanStart(it)}, ${spans.getSpanEnd(it)} / ${when(spans.getSpanFlags(it)){
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE -> "ExEx"
                                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE -> "ExIn"
                                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE -> "InEX"
                                    Spanned.SPAN_INCLUSIVE_INCLUSIVE -> "InIn"
                                    else -> spans.getSpanFlags(it)

                                }                                }")
                            }
                        }
                        if(initalEdit){
                            initalEdit=false
                        }else{
                            onMemoChangeListener?.onMemoChange(lastTextHistoryItem?.data as SpannableStringBuilder? ?:SpannableStringBuilder(""))
                        }
                    }


                    if(s.isEmpty()){
                        initText = true
                    }
                }

            })



            setOnTouchListener { v, event ->
                if(drawActivate){
                    (parent as View).onTouchEvent(event)
                    true
                }else{
                    false
                }
            }
        }

    }

    var isScaling = false

    private var initalEdit = false

    fun initialMemo(memo:SpannableStringBuilder,draw:List<CheckItem>){
        var tzidx = 0
        val textZIdx = HashMap<Int,Int>()

        var dzidx = 0
        val drawZIdx = HashMap<Int,Int>()

        var bzidx = 0
        val bitmapZIdx = HashMap<Int,Int>()



        changedCurEditable = true
        initalEdit = true
        fixedString = memo
        fixedDrawList = draw.toMutableList()
        fixedDrawList.forEach {
            when(it.type){
                MemoType.TextBox -> {
                    val (text,rectf,paint) = it.data as Triple<String,RectF,Paint>
                    textList+=text
                    textRectFList+=rectf
                    textPaintList+=paint
                    textboxHistory[tzidx] = ArrayList()
                    textboxHistory[tzidx]!!.add(Triple(String(textList[tzidx].toCharArray()),RectF(textRectFList[tzidx]),Paint(textPaintList[tzidx])))
                    textZIdx.put(it.zidx,tzidx++)

                }
                MemoType.Draw -> {
                    val (path,paint) = it.data as Pair<Path,Paint>
                    drawList+=path
                    drawPaintList+=paint
                    drawZIdx.put(it.zidx,dzidx++)
                }
                MemoType.Bitmap -> {
                    val (bitmap,rectf) = it.data as Pair<Bitmap,RectF>
                    bitmaps+=bitmap
                    bitmapRectFs+=rectf
                    bitmapHistory[bzidx] = ArrayList()
                    bitmapHistory[bzidx]!!.add(bitmapRectFs[bzidx])
                    bitmapZIdx.put(it.zidx,bzidx++)

                }
                MemoType.Text -> null
                MemoType.Default -> null
            }
        }
        for(i in 0..fixedDrawList.size-1){
            fixedDrawList[i] = fixedDrawList[i].let{
                val newZidx = when(it.type){
                    MemoType.TextBox -> textZIdx[it.zidx]
                    MemoType.Draw -> drawZIdx[it.zidx]
                    MemoType.Bitmap -> bitmapZIdx[it.zidx]
                    MemoType.Text -> null
                    MemoType.Default -> null
                }
                it.copy(zidx = newZidx?:0)
            }
        }
        invalidate()
    }



    private var isKeypadOn = false

    init {
        addView(bitmapMenu.root)
        addView(textboxMenu.root)
        addView(textMain)


        //TODO("새 메모 추가시 이부분이 없어도 되는지 체크")
        //settingEditable()

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

    fun deepCopyOfSpannableStringBuilder(spannableStringBuilder: SpannableStringBuilder):SpannableStringBuilder{
        val newSpannable = SpannableStringBuilder(spannableStringBuilder.toString())

        val spans = spannableStringBuilder.getSpans(0, spannableStringBuilder.length, Any::class.java)

        for (span in spans) {
            val start = spannableStringBuilder.getSpanStart(span)
            val end = spannableStringBuilder.getSpanEnd(span)
            val flags = spannableStringBuilder.getSpanFlags(span)

            when(span){
                is AbsoluteSizeSpan -> {
                    // AbsoluteSizeSpan 복제
                    val copiedSpan = AbsoluteSizeSpan(span.size, span.dip)
                    newSpannable.setSpan(copiedSpan, start, end, flags)
                }
                is ForegroundColorSpan -> {
                    // ForegroundColorSpan 복제
                    val copiedSpan = ForegroundColorSpan(span.foregroundColor)
                    newSpannable.setSpan(copiedSpan, start, end, flags)
                }
                is StyleSpan -> {
                    // StyleSpan 복제
                    val copiedSpan = StyleSpan(span.style)
                    newSpannable.setSpan(copiedSpan, start, end, flags)
                }
            }
        }

        Log.d("히스토리 복사 성공여부",(newSpannable!==spannableStringBuilder).toString())

        return newSpannable
    }


    fun modifyEditableStyle(s:Int, e:Int, editable: Editable?, modifiedStyle:StringStyle){
        if(s<0 || e<0) return
        Log.d("수정 진입",editable.toString()+": ${s},${e} / "+modifiedStyle.toString())
        editable?.run {
            Log.d("스타일 px값",modifiedStyle.size.toString())
            getSpans(s, e, Any::class.java).forEach {
                if(it is AbsoluteSizeSpan||it is StyleSpan||it is ForegroundColorSpan){



                    val start = getSpanStart(it)
                    val end = getSpanEnd(it)

                    removeSpan(it)
                    Log.d("수정 스팬범위","${start},${end}")
                    if(start<s){
                        when(it){
                            is AbsoluteSizeSpan -> setSpan(AbsoluteSizeSpan(it.size, it.dip), start, s,Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                            is StyleSpan -> setSpan(StyleSpan(it.style), start,s,Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                            is ForegroundColorSpan -> setSpan(ForegroundColorSpan(it.foregroundColor), start,s,Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        }
                    }
                    if(end>e){
                        when(it){
                            is AbsoluteSizeSpan -> setSpan(AbsoluteSizeSpan(it.size, it.dip), e,end,Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                            is StyleSpan -> setSpan(StyleSpan(it.style), e,end,Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                            is ForegroundColorSpan -> setSpan(ForegroundColorSpan(it.foregroundColor), e,end,Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        }
                    }
                }
            }

            val abSpan = AbsoluteSizeSpan(modifiedStyle.size.toInt(),false)
            val stSpan = StyleSpan(if(modifiedStyle.isBold) Typeface.BOLD else Typeface.NORMAL)
            val foreSpan = ForegroundColorSpan(modifiedStyle.color)

            setSpan(abSpan,s,e, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            setSpan(stSpan,s,e, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            setSpan(foreSpan,s,e,Spannable.SPAN_EXCLUSIVE_INCLUSIVE)

        }

    }

    data class StringStyle(
        val size:Float,
        val isBold:Boolean,
        val color:Int
    )

    fun setBold(isBold: Boolean){
        this.isBold = isBold

        settingEditable()
    }

    fun setTextSize(size:Float){
        textSize = size
        if(isSettingAutoChagned){
            Log.d("오토 세팅","true")
            isSettingAutoChagned = false
            return
        }
        settingEditable()
    }

    fun setTextColor(color:Int){
        textColor = color

        settingEditable()
    }



    fun saveHistoryOfEditable(editable: Editable?, isTextHistoryBack:Boolean = false){

        editable?.let {
            drawHistory.removeIf { it.action==ActionType.InitText }
            val newEditable = deepCopyOfSpannableStringBuilder(it as SpannableStringBuilder)
            Log.d("새 eidtable추가",newEditable.toString())
            secondLastTextHistoryItem = lastTextHistoryItem
            lastTextHistoryItem =HistoryItem(ActionType.ModifyText,zidxText++,newEditable)
            addHistory(lastTextHistoryItem!!,isTextHistoryBack)
            Log.d("추가지점 히스토리",drawHistory.toString())
            if(drawHistory.size>1){
                Log.d("히스토리 참조체크",(drawHistory[0]===drawHistory[1]).toString())
            }

        }
    }

    fun settingEditable(){
        val s = textMain.selectionStart
        val e = textMain.selectionEnd

        Log.d("히스토리 범위","${s},${e}")
        //범위 지정 변경
        if(s!=e){
            modifyRangeText(s,e,lastTextHistoryItem?.data as SpannableStringBuilder)
            modifyRangeText(s,e,textMain.text)

            CoroutineScope(Dispatchers.IO).launch{
                onMemoChangeListener?.onMemoChange(lastTextHistoryItem?.data as SpannableStringBuilder)
            }
        }
        //단일 서식 변경
        //새 히스토리 추가 2
        else{
            if(secondLastTextHistoryItem?.data.toString() != lastTextHistoryItem?.data.toString()||secondLastTextHistoryItem==null){
                Log.d("히스토리 추가","2")
                isUnitSettingChanged = true
                saveHistoryOfEditable(lastTextHistoryItem?.data as SpannableStringBuilder?)
            }
        }
    }




    //시작 텍스트가 없는 상태에서 지정하면 오류가 나기때문에 첫 글자가 타이핑되는 타이밍에 서식을 지정해야함
    fun modifyRangeText(s:Int,e:Int, editable: Editable?){
        val style = StringStyle(dpToPx(this@MemoView.textSize),isBold, textColor)
        Log.d("스타일 적용전 px값","${this@MemoView.textSize} -> ${style.size}")
        modifyEditableStyle(s,e,editable,style)

    }

    fun setTextDrawMode(isDrawActive:Boolean){
        isSettingChanged = true
        removeActivate()
        drawActivate=isDrawActive
        textMain.clearFocus()
        if(!isDrawActive) {
            outerFocusTextBox = false
            outerFocusTitle = false
            textMain.requestFocus()
        }else{
            textMain.clearFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(textMain.windowToken, 0)
        }
    }

    fun removeActivate(){
        activatedIdx = null
        activateTextBox = null
        bitmapMenu.root.visibility = View.GONE
        textboxMenu.root.visibility = View.GONE
        invalidate()
    }

    private fun addHistory(historyItem: HistoryItem,isTextHistoryBack: Boolean = false){
        if(curDrawHistory!=drawHistory.size-1){
            for(i in curDrawHistory+1..drawHistory.size-1){
                drawHistory.removeLast()
            }
        }
        drawHistory.addLast(historyItem)
        curDrawHistory=drawHistory.size-1

        if(curDrawHistory>50){
            val gen = generateFilteredList(0)
            fixedDrawList = gen.first
            fixedString = gen.second?.data as SpannableStringBuilder
            drawHistory.removeFirst()
            curDrawHistory--
        }
        if(isTextHistoryBack){
            curDrawHistory--
        }


        onActivateHistoryBtnListener?.onActivateHistoryBtn(drawHistory.size-1,curDrawHistory)

    }

    fun historyGoBack(){
        textMain.clearFocus()
        historyMove = true
        Log.d("현재 인덱스 전","${curDrawHistory} / ${drawHistory.size-1}")
        if(curDrawHistory>=0){

            changedCurEditable = true
            curDrawHistory--
            activateTextBox = null
            activatedIdx = null
            Log.d("현재 인덱스 후","${curDrawHistory} / ${drawHistory.size-1}")
            onActivateHistoryBtnListener?.onActivateHistoryBtn(drawHistory.size-1,curDrawHistory)

            invalidate()

        }



    }

    fun historyGoAfter(){
        textMain.clearFocus()
        if(curDrawHistory<drawHistory.size-1){

            changedCurEditable = true

            curDrawHistory++
            activateTextBox = null
            activatedIdx = null
            historyMove = true
            onActivateHistoryBtnListener?.onActivateHistoryBtn(drawHistory.size-1,curDrawHistory)

            invalidate()
        }
    }

    fun addTextBox(){
        val idx = textList.size
        textList+=""
        textPaintList+=Paint().apply {
            textSize = dpToPx(24f)
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
                    Log.d("메모 수정",text)
                    textList[it]=text
                    addHistory(HistoryItem(ActionType.ModifyTextBox,it,textboxHistory[it]?.size))
                    textboxHistory[it]!!.add(Triple(String(textList[it].toCharArray()),RectF(textRectFList[it]),Paint(textPaintList[it])))

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
            top = dpToPx(80f)
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
            bitmapRectFs+=RectF((width- dpToPx(100f))/2f,30f, (width- dpToPx(100f))/2f+ dpToPx(100f),30f+it.height.toFloat()* dpToPx(100f) /it.width.toFloat())
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
            strokeWidth = dpToPx(penSize.toFloat())
        }
        addHistory(HistoryItem(ActionType.AddDraw, drawList.size-1,Pair(drawList.last(),drawPaintList.last())))
    }

    fun removeDraw(x:Float?,y:Float?){
        if(x==null||y==null) return

        val radius = 20f

        val erased = drawHistory.filterIndexed { idx,it -> idx<=curDrawHistory&&it.action==ActionType.EraseDraw }
        val draw = drawHistory.filterIndexed{idx,it -> idx<=curDrawHistory&&it.action==ActionType.AddDraw}.toMutableList()

        fixedDrawList.forEach {
            if(it.type==MemoType.Draw){
                draw+=HistoryItem(ActionType.AddDraw,it.zidx,it.data)
            }
        }

        drawList.forEachIndexed { idx, it ->

            if(erased.filter { it.zidx==idx }.isEmpty()&&draw.filter { it.zidx==idx }.isNotEmpty()){
                val pathMeasure = PathMeasure(it,false)
                val pathLength = pathMeasure.length
                val position = FloatArray(2)
                var distance = 0f

                //distance는 path의 시작지점부터 이동거리가 distance인 지점의 좌표를 찾기위함
                //즉 모든 좌표를 순회하며 터치지점과 거리가 20f인 점이 있는지 찾는것
                while(distance < pathLength){
                    pathMeasure.getPosTan(distance, position, null)
                    val px = position[0]
                    val py = position[1]

                    val dist = Math.sqrt(((px-x)*(px-x)+(py-y)*(py-y)).toDouble())

                    if(dist <= radius){
                        addHistory(HistoryItem(ActionType.EraseDraw,idx,Pair(drawList[idx],drawPaintList[idx])))
                        break
                    }
                    distance++
                }
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
        val list= generateFilteredList().first
        list.forEach {
            if(it.type== MemoType.TextBox){
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
            }else if(it.type== MemoType.Bitmap){
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



    //true면 고정 히스토리 제너레이트
    private fun generateFilteredList(upper:Int = curDrawHistory, type:Boolean = false):Triple<ArrayList<CheckItem>,HistoryItem?,HistoryItem?>{
        Log.d("메모 추가 히스토리",drawHistory.toString())
        val list = ArrayList<CheckItem>(fixedDrawList)
        var string :HistoryItem? =null
        var stringPrev:HistoryItem? = null
        drawHistory.filterIndexed { index, historyItem -> index<=upper }.forEach {
            when(it.action){
                ActionType.EraseTextBox -> list.remove(CheckItem(MemoType.TextBox, textboxHistory[it.zidx]!!.last(),it.zidx))
                ActionType.EraseDraw -> list.remove(CheckItem(MemoType.Draw,it.data,it.zidx))
                ActionType.EraseBitmap -> list.remove(CheckItem(MemoType.Bitmap,it.data,it.zidx))
                ActionType.AddTextBox -> list+= CheckItem(MemoType.TextBox,textboxHistory[it.zidx]!![0],it.zidx)
                ActionType.AddDraw -> list+= CheckItem(MemoType.Draw,it.data,it.zidx)
                ActionType.AddBitmap -> list+= CheckItem(MemoType.Bitmap,Pair(bitmaps[it.zidx],bitmapHistory[it.zidx]!![0]),it.zidx)
                ActionType.ModifyText -> {
                    stringPrev = string
                    string = it
                }
                ActionType.ModifyTextBox -> {
                    val hisIdx = it.data as Int
                    val listItem = list.filter{item -> item.zidx==it.zidx&&item.type== MemoType.TextBox }[0]
                    val listIdx = list.indexOf(listItem)
                    list[listIdx] = CheckItem(MemoType.TextBox, textboxHistory[it.zidx]!![hisIdx],it.zidx)
                }
                ActionType.ModifyBitamp -> {
                    val hisIdx = it.data as Int
                    val listItem = list.filter{item ->item.zidx == it.zidx&&item.type == MemoType.Bitmap }[0]
                    val listIdx = list.indexOf(listItem)
                    list[listIdx] = CheckItem(MemoType.Bitmap, Pair(bitmaps[it.zidx],bitmapHistory[it.zidx]!![hisIdx]),it.zidx)
                }

                ActionType.InitText -> {} //처음 텍스트작성시 히스토리 생성용 더미 값
            }
        }

        latestList = list
        CoroutineScope(Dispatchers.IO).launch{
            onDrawChangeListener?.onDrawChange(latestList)
        }

        return Triple(list,string,stringPrev)
    }

    override fun dispatchDraw(canvas: Canvas) {

        val (list,string,stringPrev) =generateFilteredList()
        list.forEach {
            if(it.type== MemoType.Bitmap &&it.zidx == activatedIdx?:-2){
                canvas.drawBitmap(bitmaps[it.zidx],null,bitmapRectFs[it.zidx],bitmapPaint)
            }else if(it.type== MemoType.TextBox &&it.zidx==activateTextBox?:-2){
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

        lastTextHistoryItem = string
        secondLastTextHistoryItem = stringPrev
        if(changedCurEditable){
            post{
                fixedString.getSpans(0,fixedString.length,Any::class.java).forEach {
                    Log.d("스팬들",it.toString()+":${fixedString.getSpanStart(it)}, ${fixedString.getSpanEnd(it)}")
                }
                textMain.text = (lastTextHistoryItem?.data as SpannableStringBuilder?)?:fixedString
                changedCurEditable = false
            }

        }
        super.dispatchDraw(canvas)



    }

    private var touchTime = 0L
    var isAnimatorActive = false

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        event?.let { event ->

            if(event.action == MotionEvent.ACTION_DOWN){
                touchTime = System.currentTimeMillis()
                Log.d("이벤트체크1","1")
                (context as MemoActivity).binding?.let{
                    if(it.memoEtAddTextBox.isFocused||it.memoEtTitle.isFocused){
                        it.memoEtAddTextBox.clearFocus()
                        it.memoEtTitle.clearFocus()
                    }
                    if(!textMain.isFocused&&!drawActivate) textMain.requestFocus()
                }
            }

            if(!drawActivate&&event.action ==MotionEvent.ACTION_DOWN){
                return true
            }

            if(!drawActivate&&event.action == MotionEvent.ACTION_UP){
                (context as MemoActivity).binding.memoEtTitle.clearFocus()

                if(System.currentTimeMillis() - touchTime > 150||isAnimatorActive){
                    return false
                }

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
                    onActivateHistoryBtnListener?.onActivateHistoryBtn(drawHistory.size-1,curDrawHistory)
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
                            bitmapMenu.root.post{
                                val x = (bitmapRectFs[it1].left+bitmapRectFs[it1].right)/2f - bitmapMenuWidth/2f
                                bitmapMenu.root.x= if(x+bitmapMenuWidth>width){
                                    width - bitmapMenuWidth
                                }else if(x<0){
                                    0f
                                }else{
                                    x
                                }
                                val y= bitmapRectFs[it1].top - dpToPx(50f) - bitmapMenu.root.height - dpToPx(15f)/scaleX
                                bitmapMenu.root.y=if(y<0) 0f else y

                                addHistory(HistoryItem(ActionType.ModifyBitamp,it1,bitmapHistory[it1]?.size))
                                bitmapHistory[it1]!!.add(RectF(bitmapRectFs[it1]))
                            }

                        }

                        activateTextBox?.let { it2 ->
                            textboxMenu.root.visibility = View.VISIBLE
                            textboxMenu.root.post{
                                val x =(textRectFList[it2].left+textRectFList[it2].right)/2f - textMenuWidth/2f
                                textboxMenu.root.x= if(x+textMenuWidth>width){
                                    width -textMenuWidth
                                }else if(x<0){
                                    0f
                                }else{
                                    x
                                }
                                val y = textRectFList[it2].top - textboxMenu.root.height - dpToPx(15f)/scaleX
                                textboxMenu.root.y = if(y<0) 0f else y

                                addHistory(HistoryItem(ActionType.ModifyTextBox,it2,textboxHistory[it2]?.size))
                                textboxHistory[it2]!!.add(Triple(String(textList[it2].toCharArray()),RectF(textRectFList[it2]),Paint(textPaintList[it2])))
                            }

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
    ModifyText(2),
    InitText(3)

}

data class HistoryItem(
    val action: ActionType,
    val zidx: Int,
    var data:Any?
)

data class CheckItem(
    val type: MemoType,
    val data:Any?,
    val zidx: Int
)

data class ModifiedMotionEvent(
    val action: Int,
    val x:Float,
    val y:Float
)

