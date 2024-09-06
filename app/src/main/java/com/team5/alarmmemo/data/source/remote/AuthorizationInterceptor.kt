package com.team5.alarmmemo.data.source.remote

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthorizationInterceptor @Inject constructor(): Interceptor{
    private val ID = "eijuta9wl4"
    private val KEY = "2QlcWu0aidAGtxLfZ40kqgipUI0rrtof8PXBgNXr"

    override fun intercept(chain: Interceptor.Chain): Response {
        val originRequest = chain.request()
        val newUrl = originRequest.url.newBuilder().addQueryParameter("X-NCP-APIGW-API-KEY-ID",ID).addQueryParameter("X-NCP-APIGW-API-KEY",KEY).build()
        val newRequest = originRequest.newBuilder().url(newUrl).build()
        return chain.proceed(newRequest)
    }
}