package com.team5.alarmmemo.data.source.remote

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(ViewModelComponent::class)
object RestApiModule {
    private const val BASE_URL = "https://naveropenapi.apigw.ntruss.com/"

    @Provides
    fun provideGson(): GsonConverterFactory{
        return GsonConverterFactory.create()
    }

    @Provides
    fun provideOkHttpClient(authorizationInterceptor: AuthorizationInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authorizationInterceptor)
            .addNetworkInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }).build()
    }

    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder().baseUrl(BASE_URL).client(okHttpClient)
            .addConverterFactory(gsonConverterFactory).build()
    }

    @Provides
    fun provideGeocode(retrofit: Retrofit):GeocodeResult{
        return retrofit.create(GeocodeResult::class.java)
    }

    @Provides
    fun provideReverseGeocode(retrofit: Retrofit):ReverseGeocodeResult{
        return retrofit.create(ReverseGeocodeResult::class.java)
    }
}