package com.example.demoapp.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    /**
     * ВАЖНО — адрес бэкенда.
     *
     * "http://10.0.2.2:8080/"  — РАБОТАЕТ ТОЛЬКО В ЭМУЛЯТОРЕ.
     *   10.0.2.2 — это специальный алиас внутри эмулятора, указывающий на localhost
     *   твоего компьютера. На реальном телефоне такого адреса нет, поэтому приложение
     *   "не работает на телефоне".
     *
     * Чтобы работало на ФИЗИЧЕСКОМ телефоне:
     *   1. Телефон и компьютер должны быть в ОДНОЙ Wi-Fi сети.
     *   2. Узнай локальный IP компьютера:
     *        Windows: ipconfig  -> "IPv4-адрес" (например 192.168.1.42)
     *        Mac/Linux: ifconfig | grep inet  или  ip addr
     *   3. Подставь его сюда, например: "http://192.168.1.42:8080/"
     *   4. На бэкенде Spring разреши слушать все интерфейсы (см. инструкцию FIREBASE_SETUP.md).
     *   5. Брандмауэр Windows должен пропускать входящие на порт 8080.
     *
     * Совет: вынеси выбор адреса в BuildConfig или экран настроек, чтобы переключаться
     * между эмулятором (10.0.2.2) и телефоном (IP в сети) без правки кода.
     */
    @Volatile
    var BASE_URL: String = "http://192.168.1.42:8080/"   // ← ЗАМЕНИ на IP своего ПК
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
