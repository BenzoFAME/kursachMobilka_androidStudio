package com.example.demoapp.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.data.api.RetrofitClient
import com.example.demoapp.data.model.CreateWallPostRequest
import com.example.demoapp.data.model.WallPostDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WallViewModel : ViewModel() {
    private val _posts = MutableStateFlow<List<WallPostDto>>(emptyList())
    val posts: StateFlow<List<WallPostDto>> = _posts

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadPosts() = viewModelScope.launch {
        try {
            val response = RetrofitClient.api.getWallPosts()
            if (response.isSuccessful) _posts.value = response.body() ?: emptyList()
            else _error.value = "Ошибка загрузки стены"
        } catch (e: Exception) {
            _error.value = "Ошибка: ${e.message}"
        }
    }

    fun createPost(content: String) = viewModelScope.launch {
        try {
            val response = RetrofitClient.api.createWallPost(CreateWallPostRequest(content))
            if (response.isSuccessful) loadPosts()
            else _error.value = "Ошибка публикации"
        } catch (e: Exception) {
            _error.value = "Ошибка: ${e.message}"
        }
    }

    fun deletePost(id: Long) = viewModelScope.launch {
        try {
            RetrofitClient.api.deleteWallPost(id)
            loadPosts()
        } catch (e: Exception) {
            _error.value = "Ошибка удаления: ${e.message}"
        }
    }
}