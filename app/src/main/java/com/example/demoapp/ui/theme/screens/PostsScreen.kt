package com.example.demoapp.ui.theme.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demoapp.data.model.PostDto
import com.example.demoapp.ui.theme.viewmodel.PostsViewModel
import com.example.demoapp.ui.theme.viewmodel.PostsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostsScreen(
    channelId: Long,
    onOpenPost: (Long) -> Unit,
    onBack: () -> Unit,
    vm: PostsViewModel = viewModel(factory = PostsViewModelFactory(channelId))
) {
    val posts by vm.posts.collectAsState()
    val error by vm.error.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.loadPosts() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Посты") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Создать пост")
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (posts.isEmpty()) {
                Text(
                    "Постов нет. Создайте первый!",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn {
                    items(posts) { post ->
                        PostItem(
                            post = post,
                            onClick = { onOpenPost(post.id) },
                            onDelete = { vm.deletePost(post.id) }
                        )
                        HorizontalDivider()
                    }
                }
            }

            error?.let {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                ) { Text(it) }
            }
        }
    }

    if (showDialog) {
        CreatePostDialog(
            onDismiss = { showDialog = false },
            onCreate = { content ->
                vm.createPost(content)
                showDialog = false
            }
        )
    }
}

@Composable
fun PostItem(post: PostDto, onClick: () -> Unit, onDelete: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(post.content, maxLines = 3) },
        supportingContent = { Text(post.createdAt, style = MaterialTheme.typography.labelSmall) }, // ← было authorEmail
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить пост")
            }
        }
    )
}

@Composable
fun CreatePostDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый пост") },
        text = {
            OutlinedTextField(
                value = content, onValueChange = { content = it },
                label = { Text("Текст поста") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        },
        confirmButton = {
            Button(onClick = { if (content.isNotBlank()) onCreate(content) }) { Text("Опубликовать") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}