// File: app/src/main/java/com/example/inventorytokokotlinumb/network/RetrofitClient.kt
package com.ahmar.inventorytokokotlinumb.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Ganti dengan IP Address komputer Anda atau "10.0.2.2" untuk emulator Android Studio
    private const val BASE_URL = "http://10.0.2.2:3000/api/"

    private var authToken: String? = null

    // Fungsi untuk mengatur token otorisasi
    fun setAuthToken(token: String?) {
        this.authToken = token
    }

    // Interceptor untuk menambahkan header Authorization
    private val authInterceptor = okhttp3.Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
        authToken?.let {
            requestBuilder.header("Authorization", "Bearer $it")
        }
        requestBuilder.method(original.method, original.body)
        chain.proceed(requestBuilder.build())
    }

    // Interceptor untuk logging HTTP requests/responses
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Log request dan response bodies
    }

    // Konfigurasi OkHttpClient
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor) // Tambahkan interceptor autentikasi
        .addInterceptor(loggingInterceptor) // Tambahkan interceptor logging
        .connectTimeout(30, TimeUnit.SECONDS) // Waktu tunggu koneksi
        .readTimeout(30, TimeUnit.SECONDS) // Waktu tunggu baca data
        .writeTimeout(30, TimeUnit.SECONDS) // Waktu tunggu tulis data
        .build()

    // Inisialisasi Retrofit
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Gunakan OkHttpClient yang sudah dikonfigurasi
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Instance ApiService yang akan digunakan di seluruh aplikasi
    val instance: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
