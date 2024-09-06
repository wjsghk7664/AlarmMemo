package com.team5.alarmmemo.data.model



data class Geocode(
    val status: String?,
    val meta: Meta?,
    val addresses:List<Address>?,
    val errorMessage:String?
)

data class Meta(
    val totalCount:Int?,
    val page:Int?,
    val count:Int?
)

data class Address(
    val roadAddress: String?="",
    val x:String?="",
    val y:String?=""
)


