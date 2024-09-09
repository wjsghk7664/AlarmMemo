package com.team5.alarmmemo.presentation.memo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.team5.alarmmemo.UiState
import com.naver.maps.geometry.LatLng
import com.team5.alarmmemo.data.repository.LocationConvertingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchAddressViewModel @Inject constructor(private val locationConvertingRepository:LocationConvertingRepository):ViewModel() {
    private val _uiState = MutableStateFlow<UiState<Pair<LatLng, String>>>(UiState.Init)
    val uiState = _uiState.asStateFlow()

    fun searchByAddress(address: String?){
        if(address==null){
            _uiState.value = UiState.Failure("검색 결과가 존재하지 않습니다.")
        }else{
            viewModelScope.launch {
                val location =locationConvertingRepository.AddressToLatLng(address).getOrNull()
                Log.d("로케이션addr",location.toString())
                if(location == null){
                    _uiState.value = UiState.Failure("오류로 인해 검색에 실패하였습니다.")
                }else{
                    val latLng = LatLng(location.addresses?.get(0)?.y?.toDouble()?:0.0, location.addresses?.get(0)?.x?.toDouble()?:0.0)
                    _uiState.value = UiState.Success(Pair(latLng,address))
                }
            }
        }
    }

    fun searchByLatLng(latLng: LatLng?){
        if(latLng==null){
            _uiState.value = UiState.Failure("좌표가 존재 하지 않습니다.")
        }else{
            viewModelScope.launch {
                val location = locationConvertingRepository.LatLngToAddress(latLng).getOrNull()
                Log.d("로케이션latlng",location.toString())
                if(location ==null){
                    _uiState.value = UiState.Failure("오류로 인해 검색에 실패하였습니다.")
                }else{
                    var result = ""
                    location.results?.get(0)?.run{
                        result+=region?.area1?.name?:""
                        if(region?.area1?.name?:"" != "") result+=" "
                        result+=region?.area2?.name?:""
                        if(region?.area2?.name?:"" != "") result+=" "
                        result+=region?.area3?.name?:""
                        if(region?.area3?.name?:"" != "") result+=" "
                        result+=region?.area4?.name?:""
                        if(region?.area4?.name?:"" != "") result+=" "
                        result+=land?.name?:""
                    }
                    _uiState.value = UiState.Success(Pair(latLng,result))
                }
            }
        }
    }

}