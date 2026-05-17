package com.example.demoapp.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.data.api.RetrofitClient
import com.example.demoapp.data.model.ChannelDto
import com.example.demoapp.data.model.CreateChannelRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChannelsViewModel : ViewModel() {
    private val _channels = MutableStateFlow<List<ChannelDto>>(emptyList())
    val channels: StateFlow<List<ChannelDto>> = _channels

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadChannels() = viewModelScope.launch {
        try {
            val response = RetrofitClient.api.getChannels()
            if (response.isSuccessful) _channels.value = response.body() ?: emptyList()
            else _error.value = "Ошибка загрузки каналов"
        } catch (e: Exception) {
            _error.value = "Ошибка соединения: ${e.message}"
        }
    }

    fun createChannel(name: String, description: String) = viewModelScope.launch {
        try {
            val response = RetrofitClient.api.createChannel(CreateChannelRequest(name, description))
            if (response.isSuccessful) loadChannels()
            else _error.value = "Ошибка создания канала"
        } catch (e: Exception) {
            _error.value = "Ошибка: ${e.message}"
        }
    }

    fun deleteChannel(id: Long) = viewModelScope.launch {
        try {
            RetrofitClient.api.deleteChannel(id)
            loadChannels()
        } catch (e: Exception) {
            _error.value = "Ошибка удаления: ${e.message}"
        }
    }

    fun subscribe(id: Long) = viewModelScope.launch {
        try {
            RetrofitClient.api.subscribe(id)
            loadChannels()
        } catch (e: Exception) {
            _error.value = "Ошибка подписки: ${e.message}"
        }
    }
}