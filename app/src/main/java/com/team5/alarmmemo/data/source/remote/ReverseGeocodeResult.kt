package com.team5.alarmmemo.data.source.remote

import com.team5.alarmmemo.data.model.ReverseGeocode
import retrofit2.http.GET
import retrofit2.http.Query

interface ReverseGeocodeResult {
    @GET("map-reversegeocode/v2/gc")
    suspend fun getReverseGeocodeResult(
        @Query("coords") coords:String,
        @Query("output") output:String = "json"
    ):ReverseGeocode
}