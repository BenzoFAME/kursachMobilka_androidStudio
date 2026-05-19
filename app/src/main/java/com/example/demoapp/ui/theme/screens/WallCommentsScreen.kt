package com.example.demoapp.ui.theme.screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demoapp.data.api.RetrofitClient
import com.example.demoapp.data.model.CommentDto
import com.example.demoapp.data.model.CreateCommentRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WallCommentsViewModel(private val wallPostId: Long) : ViewModel() {
    private val _comments = MutableStateFlow<List<CommentDto>>(emptyList())
    val comments: StateFlow<List<CommentDto>> = _comments
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun load() = viewModelScope.launch {
        try {
            val r = RetrofitClient.api.getWallComments(wallPostId)
            if (r.isSuccessful) _comments.value = r.body() ?: emptyList()
            else _error.value = "Ошибка загрузки"
        } catch (e: Exception) { _error.value = e.message }
    }

    fun createComment(content: String) = viewModelScope.launch {
        try {
            val r = RetrofitClient.api.createWallComment(wallPostId, CreateCommentRequest(content))
            if (r.isSuccessful) load()
            else _error.value = "Ошибка отправки"
        } catch (e: Exception) { _error.value = e.message }
    }
}

class WallCommentsViewModelFactory(private val id: Long) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST") return WallCommentsViewModel(id) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallCommentsScreen(
    wallPostId: Long,
    onBack: () -> Unit,
    onOpenProfile: (String) -> Unit,
    vm: WallCommentsViewModel = viewModel(factory = WallCommentsViewModelFactory(wallPostId))
) {
    val comments by vm.comments.collectAsState()
    var text by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Комментарии") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        bottomBar = {
            Row(Modifier.fillMaxWidth().padding(12.dp, 8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = text, onValueChange = { text = it },
                    placeholder = { Text("Комментарий...") }, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { if (text.isNotBlank()) { vm.createComment(text); text = "" } }) {
                    Icon(Icons.Default.Send, null)
                }
            }
        }
    ) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize()) {
            items(comments) { comment ->
                ListItem(
                    headlineContent = { Text(comment.content) },
                    supportingContent = {
                        // Тут нужен email автора — если бэк его не возвращает, убери clickable
                        Text("User ${comment.userId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        IconButton(onClick = {}) { Icon(Icons.Default.Delete, null) }
                    }
                )
                HorizontalDivider()
            }
        }
    }
}