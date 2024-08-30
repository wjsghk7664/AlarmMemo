package com.team5.alarmmemo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MemoApplication: Application() {
    override fun onCreate() {
        super.onCreate()
    }
}