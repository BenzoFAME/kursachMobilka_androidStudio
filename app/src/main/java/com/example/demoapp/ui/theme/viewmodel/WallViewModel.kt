package com.example.demoapp.ui.theme.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.data.api.RetrofitClient
import com.example.demoapp.data.model.CreateWallPostRequest
import com.example.demoapp.data.model.WallPostDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

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

    fun createPost(content: String, imageUri: Uri?, context: Context) = viewModelScope.launch {
        try {
            var imageUrl: String? = null

            if (imageUri != null) {
                val bytes = context.contentResolver.openInputStream(imageUri)!!.readBytes()
                val part = MultipartBody.Part.createFormData(
                    "file", "image.jpg",
                    bytes.toRequestBody("image/jpeg".toMediaType())
                )
                val uploadResponse = RetrofitClient.api.uploadFile(part)
                if (uploadResponse.isSuccessful) {
                    imageUrl = uploadResponse.body()?.get("url")
                }
            }

            RetrofitClient.api.createWallPost(CreateWallPostRequest(content, imageUrl))
            loadPosts()
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