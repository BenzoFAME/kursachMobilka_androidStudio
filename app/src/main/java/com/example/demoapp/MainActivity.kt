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
                    val token = tokenStore.getAccessToken()
                    val email = tokenStore.getEmail()
                    if (!token.isNullOrBlank()) {
                        RetrofitClient.setToken(token)
                        Session.updateFromJwt(token)
                        email?.let { Session.setEmail(it) }
                    }
                    restored = !token.isNullOrBlank()
                }

                // restored != null — состояние восстановления завершено
                if (restored != null) {
                    AppNavigation(startLoggedIn = restored == true)
                }
            }
        }
    }
}
