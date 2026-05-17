package com.example.demoapp.ui.theme.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.demoapp.data.api.RetrofitClient
import com.example.demoapp.data.model.CreatePostRequest
import com.example.demoapp.data.model.PostDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
class PostsViewModel(private val channelId: Long) : ViewModel() {
    private val _posts = MutableStateFlow<List<PostDto>>(emptyList())
    val posts: StateFlow<List<PostDto>> = _posts

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadPosts() = viewModelScope.launch {
        try {
            val response = RetrofitClient.api.getPosts(channelId)
            if (response.isSuccessful) _posts.value = response.body() ?: emptyList()
            else _error.value = "Ошибка загрузки постов"
        } catch (e: Exception) {
            _error.value = "Ошибка: ${e.message}"
        }
    }

    fun createPost(content: String) = viewModelScope.launch {
        try {
            val response = RetrofitClient.api.createPost(channelId, CreatePostRequest(content))
            if (response.isSuccessful) loadPosts()
            else _error.value = "Ошибка создания поста"
        } catch (e: Exception) {
            _error.value = "Ошибка: ${e.message}"
        }
    }

    fun deletePost(id: Long) = viewModelScope.launch {
        try {
            RetrofitClient.api.deletePost(channelId, id)
            loadPosts()
        } catch (e: Exception) {
            _error.value = "Ошибка удаления: ${e.message}"
        }
    }
}

class PostsViewModelFactory(private val channelId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return PostsViewModel(channelId) as T
    }
}