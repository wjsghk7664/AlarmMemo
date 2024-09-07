package com.team5.alarmmemo

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.navercorp.nid.NaverIdLoginSDK
import com.team5.alarmmemo.util.DpPxClass
import com.team5.alarmmemo.util.DpPxUtil
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject


@HiltAndroidApp
class MemoApplication: Application() {

    @Inject lateinit var dpPxClass: DpPxClass

    override fun onCreate() {
        super.onCreate()

        KakaoSdk.init(this, "39ca23e670f1a6755ed9349b4552d5cd")

        NaverIdLoginSDK.initialize(this, getString(R.string.naver_client_id), getString(R.string.naver_client_secret), getString(R.string.naver_client_name))

        DpPxUtil.initialize(dpPxClass)
    }
}