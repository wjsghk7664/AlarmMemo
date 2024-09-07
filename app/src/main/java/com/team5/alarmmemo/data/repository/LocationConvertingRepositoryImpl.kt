package com.team5.alarmmemo.data.repository

import com.naver.maps.geometry.LatLng
import com.team5.alarmmemo.data.model.Geocode
import com.team5.alarmmemo.data.model.ReverseGeocode
import com.team5.alarmmemo.data.source.remote.GeocodeResult
import com.team5.alarmmemo.data.source.remote.ReverseGeocodeResult
import javax.inject.Inject

class LocationConvertingRepositoryImpl @Inject constructor(private val geocodeResult: GeocodeResult, private val reverseGeocodeResult: ReverseGeocodeResult):LocationConvertingRepository {
    override suspend fun LatLngToAddress(latLng: LatLng): Result<ReverseGeocode> {
        val coords =String.format("%f,%f",latLng.longitude,latLng.latitude)
        return runCatching {
            reverseGeocodeResult.getReverseGeocodeResult(coords)
        }
    }

    override suspend fun AddressToLatLng(address: String): Result<Geocode> {
        return runCatching {
            geocodeResult.getGeocodeResult(address)
        }
    }

}