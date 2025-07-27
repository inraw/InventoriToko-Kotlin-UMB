// File: app/src/main/java/com/ahmar/inventorytokokotlinumb/network/RetrofitClient.kt
package com.ahmar.inventorytokokotlinumb.network

import com.ahmar.inventorytokokotlinumb.api.ApiService // Pastikan ini mengarah ke ApiService Anda
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import android.util.Log // Import Log

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:3000/api/" // Sesuaikan dengan port backend Anda

    private var authToken: String? = null

    fun setAuthToken(token: String?) {
        this.authToken = token
        Log.d("RetrofitClient", "Auth token set to: ${token?.take(10)}...") // Log saat token disetel
    }

    fun getAuthToken(): String? {
        return authToken
    }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        // LOGGING: Periksa token yang digunakan oleh interceptor
        Log.d("RetrofitClient", "Auth Interceptor using token: ${authToken?.take(10)}...")

        authToken?.let { token ->
            builder.header("Authorization", "Bearer $token")
        }

        chain.proceed(builder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
