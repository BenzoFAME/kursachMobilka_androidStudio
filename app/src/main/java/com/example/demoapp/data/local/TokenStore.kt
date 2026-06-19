package com.example.demoapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

/**
 * Хранит JWT-токены и email пользователя на устройстве (DataStore),
 * чтобы при перезапуске приложения не приходилось логиниться заново.
 */
class TokenStore(private val context: Context) {

    companion object {
        private val ACCESS = stringPreferencesKey("access_token")
        private val REFRESH = stringPreferencesKey("refresh_token")
        private val EMAIL = stringPreferencesKey("email")
    }

    suspend fun save(accessToken: String, refreshToken: String?, email: String?) {
        context.dataStore.edit { prefs ->
            prefs[ACCESS] = accessToken
            refreshToken?.let { prefs[REFRESH] = it }
            email?.let { prefs[EMAIL] = it }
        }
    }

    suspend fun getAccessToken(): String? =
        context.dataStore.data.map { it[ACCESS] }.first()

    suspend fun getRefreshToken(): String? =
        context.dataStore.data.map { it[REFRESH] }.first()

    suspend fun getEmail(): String? =
        context.dataStore.data.map { it[EMAIL] }.first()

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
