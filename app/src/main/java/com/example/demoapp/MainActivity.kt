package com.example.demoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import com.example.demoapp.data.api.RetrofitClient
import com.example.demoapp.data.local.Session
import com.example.demoapp.data.local.TokenStore
import com.example.demoapp.data.model.RefreshRequest
import com.example.demoapp.ui.theme.DemoAppTheme
import com.example.demoapp.ui.theme.screens.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DemoAppTheme {
                val tokenStore = remember { TokenStore(applicationContext) }
                // restored = null пока грузим, true/false после проверки
                var restored by remember { mutableStateOf<Boolean?>(null) }

                LaunchedEffect(Unit) {
                    // При автообновлении токена (в RetrofitClient) сохраняем новые токены в DataStore
                    RetrofitClient.onTokensRefreshed = { access, refresh ->
                        Session.updateFromJwt(access)
                        kotlinx.coroutines.runBlocking {
                            tokenStore.save(access, refresh, tokenStore.getEmail())
                        }
                    }

                    val email = tokenStore.getEmail()
                    val refresh = tokenStore.getRefreshToken()
                    val savedAccess = tokenStore.getAccessToken()

                    // даём RetrofitClient refresh-токен для автообновления на лету
                    refresh?.let { RetrofitClient.setRefreshToken(it) }

                    var loggedIn = false

                    // Есть refresh-токен — сразу обновляем access (старый access живёт всего 10 минут
                    // и мог протухнуть, поэтому полагаться только на него нельзя).
                    if (!refresh.isNullOrBlank()) {
                        try {
                            val resp = RetrofitClient.api.refreshToken(RefreshRequest(refresh))
                            if (resp.isSuccessful) {
                                resp.body()?.let { body ->
                                    RetrofitClient.setToken(body.accessToken)
                                    Session.updateFromJwt(body.accessToken)
                                    email?.let { Session.setEmail(it) }
                                    tokenStore.save(body.accessToken, body.refreshToken, email)
                                    loggedIn = true
                                }
                            }
                        } catch (_: Exception) {
                            // нет сети или бэкенд недоступен — попробуем старый access ниже
                        }
                    }

                    // Если обновить не удалось (нет сети), но есть сохранённый access — используем его.
                    if (!loggedIn && !savedAccess.isNullOrBlank()) {
                        RetrofitClient.setToken(savedAccess)
                        Session.updateFromJwt(savedAccess)
                        email?.let { Session.setEmail(it) }
                        loggedIn = true
                    }

                    restored = loggedIn
                }

                // restored != null — состояние восстановления завершено
                if (restored != null) {
                    AppNavigation(startLoggedIn = restored == true)
                }
            }
        }
    }
}
