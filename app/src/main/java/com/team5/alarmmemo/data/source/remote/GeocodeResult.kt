package com.team5.alarmmemo.data.source.remote

import com.team5.alarmmemo.data.model.Geocode
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodeResult {
    @GET("map-geocode/v2/geocode")
    suspend fun getGeocodeResult(
        @Query("query") query:String,
    ):Geocode
}