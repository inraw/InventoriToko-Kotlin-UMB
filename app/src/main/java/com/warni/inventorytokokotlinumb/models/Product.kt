// File: app/src/main/java/com/example/inventorytokokotlinumb/models/Product.kt
package com.ahmar.inventorytokokotlinumb.models

import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("price")
    val price: Double, // Sudah Double
    @SerializedName("stock")
    val stock: Int,
    @SerializedName("image_url")
    val imageUrl: String?, // Sesuaikan dengan 'image_url' dari backend
    @SerializedName("created_at")
    val createdAt: String? // Sesuaikan dengan 'created_at' dari backend
) : Parcelable
