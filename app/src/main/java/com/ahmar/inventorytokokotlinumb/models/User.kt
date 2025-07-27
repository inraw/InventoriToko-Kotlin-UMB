package com.ahmar.inventorytokokotlinumb.models

import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: Int,
    @SerializedName("username")
    val username: String,
    @SerializedName("role")
    val role: String // "admin" atau "customer"
) : Parcelable