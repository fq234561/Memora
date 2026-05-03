package com.memorial.app.data.remote

import android.content.Context
import com.memorial.app.BuildConfig
import com.memorial.app.data.local.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val BASE_URL = BuildConfig.API_BASE_URL

    private var tokenManager: TokenManager? = null

    fun initialize(context: Context) {
        tokenManager = TokenManager(context.applicationContext)
    }

    fun setMockToken(token: String) {
        tokenManager?.accessToken = token
    }

    fun getAuthenticatedClient(): OkHttpClient {
        val loggingLevel = when (BuildConfig.LOG_LEVEL) {
            "BODY" -> HttpLoggingInterceptor.Level.BODY
            "HEADERS" -> HttpLoggingInterceptor.Level.HEADERS
            "BASIC" -> HttpLoggingInterceptor.Level.BASIC
            else -> HttpLoggingInterceptor.Level.NONE
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = loggingLevel
        }

        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val requestBuilder = chain.request().newBuilder()
                tokenManager?.accessToken?.let { token ->
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun getClient(): OkHttpClient {
        return getAuthenticatedClient()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
