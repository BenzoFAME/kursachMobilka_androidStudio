package com.example.demoapp.data.local

import android.util.Base64
import org.json.JSONObject

/**
 * Простое хранилище текущей сессии в памяти: email текущего пользователя.
 * email берётся из subject JWT-токена (claim "sub").
 */
object Session {
    @Volatile
    var myEmail: String? = null
        private set

    /** Достаёт email (subject) из access-токена JWT и запоминает его. */
    fun updateFromJwt(accessToken: String) {
        myEmail = decodeEmail(accessToken)
    }

    fun setEmail(email: String) { myEmail = email }

    fun clear() { myEmail = null }

    private fun decodeEmail(jwt: String): String? = try {
        val parts = jwt.split(".")
        if (parts.size < 2) null
        else {
            val payload = String(
                Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            )
            JSONObject(payload).optString("sub").ifBlank { null }
        }
    } catch (e: Exception) {
        null
    }
}
