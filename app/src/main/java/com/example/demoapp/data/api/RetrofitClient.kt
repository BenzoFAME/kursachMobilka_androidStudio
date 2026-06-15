package com.example.demoapp.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    /**
     * Адрес бэкенда.
     *
     * "http://10.0.2.2:8080/"  — это правильный адрес ДЛЯ ЭМУЛЯТОРА.
     *   10.0.2.2 — специальный алиас внутри эмулятора Android, который указывает на
     *   localhost (127.0.0.1) твоего компьютера, где запущен Spring-бэкенд.
     *   Поэтому при запуске на эмуляторе ничего менять не нужно.
     *
     * Если когда-нибудь будешь запускать на ФИЗИЧЕСКОМ телефоне:
     *   - телефон и ПК в одной Wi-Fi сети,
     *   - сюда подставить локальный IP ПК, например "http://192.168.1.42:8080/",
     *   - на бэкенде добавить server.address=0.0.0.0 и открыть порт 8080.
     */
    @Volatile
    var BASE_URL: String = "http://10.0.2.2:8080/"   // эмулятор
        set(value) {
            field = if (value.endsWith("/")) value else "$value/"
            _api = buildApi()
        }

    @Volatile
    private var token: String? = null

    @Volatile
    private var _api: ApiService? = null

    val api: ApiService
        get() = _api ?: buildApi().also { _api = it }

    fun setToken(newToken: String) {
        token = newToken
        _api = buildApi()
    }

    fun getToken(): String? = token

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
