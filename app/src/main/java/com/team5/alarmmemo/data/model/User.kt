package com.team5.alarmmemo.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val email: String = "",
    val password: String = "",
    val name: String = ""
) : Parcelable