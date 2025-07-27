package com.warni.inventorytokokotlinumb.models

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("message")
    val message: String
)
