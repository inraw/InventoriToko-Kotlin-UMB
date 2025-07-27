package com.ahmar.inventorytokokotlinumb.models

import com.google.gson.annotations.SerializedName
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    @SerializedName("cart_id")
    val cartId: Int,
    @SerializedName("product_id")
    val productId: Int,
    @SerializedName("product_name")
    val productName: String,
    @SerializedName("price")
    val price: Double, // Menggunakan Double untuk konsistensi
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("subtotal")
    val subtotal: Double // Menggunakan Double untuk konsistensi
) : Parcelable