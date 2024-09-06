package com.team5.alarmmemo

import android.app.Application
import com.team5.alarmmemo.util.DpPxClass
import com.team5.alarmmemo.util.DpPxUtil
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MemoApplication: Application() {

    @Inject lateinit var dpPxClass: DpPxClass

    override fun onCreate() {
        super.onCreate()

        DpPxUtil.initialize(dpPxClass)
    }
}