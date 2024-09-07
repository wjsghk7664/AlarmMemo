package com.team5.alarmmemo.data.model

data class ReverseGeocode(
    val status:Status?,
    val results:List<Result>?
)

data class Status(
    val code:Int?,
    val name:String?,
    val message:String?
)

data class Result(
    val region:Region?,
    val land:Land?
)

data class Region(
    val area0:Area?,
    val area1:Area?,
    val area2:Area?,
    val area3:Area?,
    val area4:Area?
)

data class Area(
    val name:String?=""
)

data class Land(
    val name: String?=""
)