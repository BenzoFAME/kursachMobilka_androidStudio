package com.example.demoapp.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.data.api.RetrofitClient
import com.example.demoapp.data.model.LoginRequest
import com.example.demoapp.data.model.RegisterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class AuthViewModel : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    fun login(email: String, password: String) = viewModelScope.launch {
        _state.value = AuthState(isLoading = true)
        try {
            val response = RetrofitClient.api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                response.body()?.let { RetrofitClient.setToken(it.accessToken) }
                _state.value = AuthState(isSuccess = true)
            } else {
                _state.value = AuthState(error = "Неверный email или пароль")
            }
        } catch (e: Exception) {
            _state.value = AuthState(error = "Ошибка соединения: ${e.message}")
        }
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
                response.body()?.let { RetrofitClient.setToken(it.accessToken) }
                _state.value = AuthState(isSuccess = true)
            } else {
                _state.value = AuthState(error = "Ошибка регистрации")
            }
        } catch (e: Exception) {
            _state.value = AuthState(error = "Ошибка соединения: ${e.message}")
        }
    }
}