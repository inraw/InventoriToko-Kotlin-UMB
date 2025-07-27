// File: app/src/main/java/com/ahmar/inventorytokokotlinumb/models/AddToCartRequest.kt
package com.ahmar.inventorytokokotlinumb.models

import com.google.gson.annotations.SerializedName

data class AddToCartRequest(
    @SerializedName("productId")
    val productId: Int,
    @SerializedName("quantity")
    val quantity: Int
)
