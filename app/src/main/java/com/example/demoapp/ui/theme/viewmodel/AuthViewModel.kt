package com.example.demoapp.ui.theme.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.data.api.RetrofitClient
import com.example.demoapp.data.local.Session
import com.example.demoapp.data.local.TokenStore
import com.example.demoapp.data.model.LoginRequest
import com.example.demoapp.data.model.RegisterRequest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class AuthViewModel(app: Application) : AndroidViewModel(app) {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    private val tokenStore = TokenStore(app)

    fun login(email: String, password: String) = viewModelScope.launch {
        _state.value = AuthState(isLoading = true)
        try {
            val response = RetrofitClient.api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    RetrofitClient.setToken(body.accessToken)
                    Session.updateFromJwt(body.accessToken)
                    Session.setEmail(email)
                    tokenStore.save(body.accessToken, body.refreshToken, email)
                }
                _state.value = AuthState(isSuccess = true)
            } else {
                _state.value = AuthState(error = "Неверный email или пароль")
            }
        } catch (e: Exception) {
            _state.value = AuthState(error = "Ошибка соединения: ${e.message}")
        }
    }

    /**
     * Выход из аккаунта: чистим токены на устройстве (DataStore),
     * сессию в памяти, токен в Retrofit и выходим из Firebase.
     */
    fun logout(onDone: () -> Unit = {}) = viewModelScope.launch {
        try {
            tokenStore.clear()
        } catch (_: Exception) {
        }
        RetrofitClient.clearToken()
        Session.clear()
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) {
            // Firebase мог быть не инициализирован — это не критично для выхода
        }
        _state.value = AuthState()
        onDone()
    }

    fun register(
        username: String, email: String,
        firstName: String, lastName: String, password: String
    ) = viewModelScope.launch {
        _state.value = AuthState(isLoading = true)
        try {
            val response = RetrofitClient.api.register(
                RegisterRequest(username, email, firstName, lastName, password)
            )
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    RetrofitClient.setToken(body.accessToken)
                    Session.updateFromJwt(body.accessToken)
                    Session.setEmail(email)
                    tokenStore.save(body.accessToken, body.refreshToken, email)
                }
                _state.value = AuthState(isSuccess = true)
            } else {
                _state.value = AuthState(error = "Ошибка регистрации")
            }
        } catch (e: Exception) {
            _state.value = AuthState(error = "Ошибка соединения: ${e.message}")
        }
    }
}
