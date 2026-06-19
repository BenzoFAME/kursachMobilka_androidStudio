package com.example.demoapp.ui.theme.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.demoapp.data.api.RetrofitClient
import com.example.demoapp.data.model.ProfileDto
import com.example.demoapp.data.model.WallPostDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileViewModel(private val email: String?) : ViewModel() {
    private val _profile = MutableStateFlow<ProfileDto?>(null)
    val profile: StateFlow<ProfileDto?> = _profile

    private val _posts = MutableStateFlow<List<WallPostDto>>(emptyList())
    val posts: StateFlow<List<WallPostDto>> = _posts

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun load() = viewModelScope.launch {
        try {
            // email == null — мой профиль; иначе — профиль другого пользователя по email
            val r = if (email == null) {
                RetrofitClient.api.getMyProfile()
            } else {
                RetrofitClient.api.getUserProfile(email)
            }
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


    fun uploadAvatar(uri: Uri, context: Context) = viewModelScope.launch {
        try {
            val bytes = context.contentResolver.openInputStream(uri)!!.readBytes()
            val part = MultipartBody.Part.createFormData(
                "file", "avatar.jpg",
                bytes.toRequestBody("image/jpeg".toMediaType())
            )
            val r = RetrofitClient.api.uploadAvatar(part)
            if (r.isSuccessful) load()
        } catch (e: Exception) {
            _error.value = "Ошибка загрузки аватарки: ${e.message}"
        }
    }
}

class ProfileViewModelFactory(private val email: String?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ProfileViewModel(email) as T
    }
}