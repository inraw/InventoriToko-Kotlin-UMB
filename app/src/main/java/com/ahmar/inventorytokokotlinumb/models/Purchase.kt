// File: app/src/main/java/com/example/inventorytokokotlinumb/models/Purchase.kt
package com.ahmar.inventorytokokotlinumb.models

import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Purchase(
    @SerializedName("purchase_id")
    val purchaseId: Int,
    @SerializedName("username")
    val username: String?, // Hanya ada di admin purchase
    @SerializedName("product_name")
    val productName: String,
    @SerializedName("image_url")
    val imageUrl: String?, // Hanya ada di customer purchase
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("total_price")
    val totalPrice: Double?, // Sudah Double?
    @SerializedName("purchase_date")
    val purchaseDate: String
) : Parcelable
