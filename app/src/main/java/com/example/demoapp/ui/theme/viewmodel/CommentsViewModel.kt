package com.example.demoapp.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.demoapp.data.api.RetrofitClient
import com.example.demoapp.data.model.CommentDto
import com.example.demoapp.data.model.CreateCommentRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommentsViewModel(private val postId: Long) : ViewModel() {
    private val _comments = MutableStateFlow<List<CommentDto>>(emptyList())
    val comments: StateFlow<List<CommentDto>> = _comments

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadComments() = viewModelScope.launch {
        try {
            val response = RetrofitClient.api.getComments(postId)
            if (response.isSuccessful) _comments.value = response.body() ?: emptyList()
            else _error.value = "Ошибка загрузки комментариев"
        } catch (e: Exception) {
            _error.value = "Ошибка: ${e.message}"
        }
    }

    fun createComment(content: String) = viewModelScope.launch {
        try {
            val response = RetrofitClient.api.createComment(postId, CreateCommentRequest(content))
            if (response.isSuccessful) loadComments()
            else _error.value = "Ошибка отправки комментария"
        } catch (e: Exception) {
            _error.value = "Ошибка: ${e.message}"
        }
    }

    fun deleteComment(id: Long) = viewModelScope.launch {
        try {
            RetrofitClient.api.deleteComment(postId, id)
            loadComments()
        } catch (e: Exception) {
            _error.value = "Ошибка удаления: ${e.message}"
        }
    }
}

class CommentsViewModelFactory(private val postId: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CommentsViewModel(postId) as T
    }
}