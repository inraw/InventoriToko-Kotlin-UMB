package com.warni.inventorytokokotlinumb.models

import com.google.gson.annotations.SerializedName

// Kelas generik untuk respons API sederhana (misalnya, hanya pesan sukses/error)
data class ApiResponse(
    @SerializedName("message")
    val message: String
)