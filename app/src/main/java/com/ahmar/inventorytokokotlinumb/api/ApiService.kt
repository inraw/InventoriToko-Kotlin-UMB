// File: app/src/main/java/com/ahmar/inventorytokokotlinumb/network/ApiService.kt
package com.ahmar.inventorytokokotlinumb.network

import com.ahmar.inventorytokokotlinumb.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Auth
    @POST("auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    // Admin Endpoints
    @GET("admin/products")
    suspend fun getAdminProducts(): Response<List<Product>>

    @POST("admin/products")
    suspend fun addProduct(@Body product: Product): Response<Void>

    @PUT("admin/products/{id}")
    suspend fun updateProduct(@Path("id") productId: Int, @Body product: Product): Response<Void>

    @DELETE("admin/products/{id}")
    suspend fun deleteProduct(@Path("id") productId: Int): Response<Void>

    @GET("admin/purchases")
    suspend fun getAdminPurchases(): Response<List<Purchase>> // PASTIKAN INI ADA DAN BENAR

    @GET("admin/users")
    suspend fun getUsers(): Response<List<User>>

    @DELETE("admin/users/{id}")
    suspend fun deleteUser(@Path("id") userId: Int): Response<Void>

    // Customer Endpoints
    @GET("customer/products")
    suspend fun getCustomerProducts(): Response<List<Product>>

    @GET("customer/cart")
    suspend fun getCustomerCart(): Response<List<CartItem>>

    @POST("customer/cart")
    suspend fun addProductToCart(@Body request: AddToCartRequest): Response<Void>

    @DELETE("customer/cart/{cartItemId}")
    suspend fun deleteCartItem(@Path("cartItemId") cartItemId: Int): Response<Void>

    @POST("customer/purchase")
    suspend fun makePurchase(): Response<Void>

    @GET("customer/purchases")
    suspend fun getCustomerPurchases(): Response<List<Purchase>>
}
