package com.team5.alarmmemo.util

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

object DpPxUtil {
    lateinit var dpPxClass: DpPxClass

    fun initialize(dpPxClass: DpPxClass){
        DpPxUtil.dpPxClass = dpPxClass
    }

    fun dpToPx(dp:Float):Float{
        return dpPxClass.dpToPx(dp)
    }

    fun pxToDp(px:Float):Float{
        return dpPxClass.pxToDp(px)
    }
}

@Singleton
class DpPxClass @Inject constructor(
    @ApplicationContext private val context: Context
){

    private var pxToDpMap = HashMap<Int,Int>() //px값으로 dp값 구하기 위한 map

    init {
        generatePxToDpMap()
    }

    fun generatePxToDpMap(){
        for(i in 4..64){
            val px = dpToPx(i.toFloat())
            pxToDpMap.put(px.toInt(),i)
        }
    }

    fun dpToPx(dp: Float): Float {
        val metrics = context.resources.displayMetrics
        Log.d("dp 밀도",metrics.density.toString())
        return dp * metrics.density
    }

    fun pxToDp(px: Float): Float {
        if(pxToDpMap.keys.contains(px.toInt())) return pxToDpMap[px.toInt()]!!.toFloat()
        val metrics = context.resources.displayMetrics
        Log.d("px밀도",metrics.density.toString())
        return px / metrics.density
    }
}