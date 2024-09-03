package com.team5.alarmmemo.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val emailOrToekn:String ="",
    val password:String = "",
    val name:String = "",
    val isDummy:Boolean = false
):Parcelable