package com.example.alarmmemo.data.model

import android.graphics.RectF
import android.net.Uri

data class MemoModel(
    val bitmaps : List<Uri> = listOf(),
    val rectfs : List<RectF> = listOf(),
    val text:String ="",
    val textboxes:List<String> = listOf(),
    val textlocation:List<Pair<Float,Float>> = listOf()
    )