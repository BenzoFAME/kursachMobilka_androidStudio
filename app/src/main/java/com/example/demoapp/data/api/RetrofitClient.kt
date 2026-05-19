package com.example.demoapp.data.api
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    @Volatile
    private var token: String? = null

    // НЕ lazy — пересоздаётся при каждом обращении после setToken
    @Volatile
    private var _api: ApiService? = null

    val api: ApiService
        get() = _api ?: buildApi().also { _api = it }

    fun setToken(newToken: String) {
        token = newToken
        _api = buildApi()  // пересоздаём с новым токеном
    }

    private fun buildApi(): ApiService {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor { chain ->
                val request = chain.request().newBuilder().apply {
                    token?.let { addHeader("Authorization", "Bearer $it") }
                }.build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}