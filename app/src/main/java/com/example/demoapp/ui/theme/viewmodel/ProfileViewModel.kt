package com.example.demoapp.ui.theme.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.demoapp.data.api.RetrofitClient
import com.example.demoapp.data.model.ProfileDto

import com.example.demoapp.data.model.WallPostDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class ProfileViewModel(private val email: String?) : ViewModel() {
    private val _profile = MutableStateFlow<ProfileDto?>(null)
    val profile: StateFlow<ProfileDto?> = _profile

    private val _posts = MutableStateFlow<List<WallPostDto>>(emptyList())
    val posts: StateFlow<List<WallPostDto>> = _posts

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun load() = viewModelScope.launch {
        try {
            val r = RetrofitClient.api.getMyProfile()
            if (r.isSuccessful) {
                _profile.value = r.body()
                _posts.value = r.body()?.posts ?: emptyList()
            } else {
                _error.value = "Ошибка ${r.code()}"
            }
        } catch (e: Exception) {
            _error.value = "Ошибка: ${e.message}"
        }
    }
}

class ProfileViewModelFactory(private val email: String?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ProfileViewModel(email) as T
    }
}