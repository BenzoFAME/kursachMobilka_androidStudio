package com.example.demoapp.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.demoapp.data.chat.ChatRepository
import com.example.demoapp.data.model.ChatConversation
import com.example.demoapp.data.model.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel чата. Один на экраны списка диалогов и переписки.
 */
class ChatViewModel(
    private val repo: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversations: StateFlow<List<ChatConversation>> = _conversations

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready

    val myUid: String? get() = repo.currentUid

    /** Войти в Firebase (по custom token бэкенда). Вызывать после успешного логина. */
    fun ensureFirebaseSignIn() = viewModelScope.launch {
        val result = repo.signInToFirebase()
        result.onSuccess { _ready.value = true }
            .onFailure { _error.value = "Не удалось войти в чат: ${it.message}" }
    }

    fun loadConversations() = viewModelScope.launch {
        repo.observeConversations()
            .catch { _error.value = "Ошибка загрузки диалогов: ${it.message}" }
            .collect { _conversations.value = it }
    }

    fun loadMessages(chatId: String) = viewModelScope.launch {
        repo.observeMessages(chatId)
            .catch { _error.value = "Ошибка загрузки сообщений: ${it.message}" }
            .collect { _messages.value = it }
    }

    /** Открыть/создать диалог с пользователем; callback получает chatId. */
    fun openConversation(
        otherUid: String,
        otherEmail: String,
        myEmail: String,
        onOpened: (String) -> Unit
    ) = viewModelScope.launch {
        try {
            val chatId = repo.openConversation(otherUid, otherEmail, myEmail)
            onOpened(chatId)
        } catch (e: Exception) {
            _error.value = "Не удалось открыть диалог: ${e.message}"
        }
    }

    /** Открыть/создать диалог зная только email собеседника. */
    fun openConversationByEmail(
        otherEmail: String,
        myEmail: String,
        onOpened: (String) -> Unit
    ) = viewModelScope.launch {
        try {
            // на случай если ещё не вошли в Firebase
            if (myUid == null) repo.signInToFirebase().getOrThrow()
            val chatId = repo.openConversationByEmail(otherEmail, myEmail)
            onOpened(chatId)
        } catch (e: Exception) {
            _error.value = "Не удалось открыть диалог: ${e.message}"
        }
    }

    fun sendMessage(chatId: String, text: String, myEmail: String) = viewModelScope.launch {
        if (text.isBlank()) return@launch
        try {
            repo.sendMessage(chatId, text.trim(), myEmail)
        } catch (e: Exception) {
            _error.value = "Не удалось отправить: ${e.message}"
        }
    }

    fun clearError() { _error.value = null }
}

class ChatViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ChatViewModel() as T
    }
}
