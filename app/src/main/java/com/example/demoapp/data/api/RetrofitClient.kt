package com.example.demoapp.data.api

import com.example.demoapp.data.model.RefreshRequest
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
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
    private var refreshToken: String? = null

    /**
     * Колбэк для сохранения новых токенов в DataStore после автообновления.
     * Устанавливается из MainActivity. (access, refresh) -> Unit
     */
    @Volatile
    var onTokensRefreshed: ((String, String) -> Unit)? = null

    @Volatile
    private var _api: ApiService? = null

    val api: ApiService
        get() = _api ?: buildApi().also { _api = it }

    fun setToken(newToken: String) {
        token = newToken
        _api = buildApi()
    }

    fun setRefreshToken(value: String?) {
        refreshToken = value
    }

    /** Полностью убирает токен (при выходе из аккаунта), чтобы больше не слать Authorization. */
    fun clearToken() {
        token = null
        refreshToken = null
        _api = buildApi()
    }

    fun getToken(): String? = token

    /**
     * Синхронно обновляет access-токен по refresh через отдельный вызов
     * (без Authenticator, чтобы не было рекурсии). Возвращает новый access или null.
     */
    @Synchronized
    private fun refreshBlocking(): String? {
        val rt = refreshToken ?: return null
        return try {
            // отдельный простой клиент без Authenticator и без Authorization-интерсептора
            val plain = OkHttpClient.Builder().build()
            val refreshApi = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(plain)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)

            val resp = kotlinx.coroutines.runBlocking {
                refreshApi.refreshToken(RefreshRequest(rt))
            }
            if (resp.isSuccessful) {
                val body = resp.body()
                if (body != null) {
                    token = body.accessToken
                    refreshToken = body.refreshToken
                    onTokensRefreshed?.invoke(body.accessToken, body.refreshToken)
                    body.accessToken
                } else null
            } else null
        } catch (e: Exception) {
            null
        }
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
            // При 401/403 пытаемся обновить токен и повторить запрос
            .authenticator(object : Authenticator {
                override fun authenticate(route: Route?, response: Response): Request? {
                    // уже пробовали обновить — не зацикливаемся
                    if (response.request.header("Authorization-Retried") != null) return null
                    val newAccess = refreshBlocking() ?: return null
                    return response.request.newBuilder()
                        .header("Authorization", "Bearer $newAccess")
                        .header("Authorization-Retried", "true")
                        .build()
                }
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
