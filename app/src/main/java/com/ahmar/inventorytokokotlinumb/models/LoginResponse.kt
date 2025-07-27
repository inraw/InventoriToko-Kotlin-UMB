package com.ahmar.inventorytokokotlinumb.models

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("token")
    val token: String?, // Token JWT
    @SerializedName("role")
    val role: String?,   // Peran pengguna
    @SerializedName("userId")
    val userId: Int?     // ID pengguna
)
