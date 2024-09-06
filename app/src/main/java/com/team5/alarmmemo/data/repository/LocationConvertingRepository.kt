package com.team5.alarmmemo.data.repository

import com.naver.maps.geometry.LatLng
import com.team5.alarmmemo.data.model.Address
import com.team5.alarmmemo.data.model.Geocode
import com.team5.alarmmemo.data.model.ReverseGeocode

interface LocationConvertingRepository {
    suspend fun LatLngToAddress(latLng: LatLng):Result<ReverseGeocode>
    suspend fun AddressToLatLng(address: String):Result<Geocode>
}