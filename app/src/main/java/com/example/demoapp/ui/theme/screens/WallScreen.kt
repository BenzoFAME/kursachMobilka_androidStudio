package com.example.demoapp.ui.theme.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demoapp.data.model.WallPostDto
import com.example.demoapp.ui.theme.viewmodel.WallViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallScreen(
    onOpenComments: (Long) -> Unit,
    onOpenProfile: (String) -> Unit,
    vm: WallViewModel = viewModel()
) {
    val posts by vm.posts.collectAsState()
    val error by vm.error.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.loadPosts() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Стена") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Написать")
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (posts.isEmpty()) {
                Text(
                    "Стена пуста. Будьте первым!",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn {
                    items(posts) { post ->
                        WallPostItem(
                            post = post,
                            onOpenComments = { onOpenComments(post.id) },
                            onOpenProfile = { onOpenProfile(post.authorEmail) },
                            onDelete = { vm.deletePost(post.id) }
                        )
                        HorizontalDivider()
                    }
                }
            }
            error?.let {
                Snackbar(Modifier.align(Alignment.BottomCenter).padding(16.dp)) { Text(it) }
            }
        }
    }

    if (showDialog) {
        CreateWallPostDialog(
            onDismiss = { showDialog = false },
            onCreate = { content -> vm.createPost(content); showDialog = false }
        )
    }
}

@Composable
fun WallPostItem(
    post: WallPostDto,
    onOpenComments: () -> Unit,
    onOpenProfile: () -> Unit,
    onDelete: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onOpenComments() },
        headlineContent = { Text(post.content, maxLines = 4) },
        supportingContent = {
            Column {
                Text(
                    "@${post.authorUsername}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onOpenProfile() }
                )
                Text(post.createdAt, style = MaterialTheme.typography.labelSmall)
                if (post.commentDto.isNotEmpty()) {
                    Text(
                        "${post.commentDto.size} комм.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить")
            }
        }
    )
}

@Composable
fun CreateWallPostDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var content by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая запись") },
        text = {
            OutlinedTextField(
                value = content, onValueChange = { content = it },
                label = { Text("Что у вас нового?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        },
        confirmButton = {
            Button(onClick = { if (content.isNotBlank()) onCreate(content) }) { Text("Опубликовать") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } }
    )
}