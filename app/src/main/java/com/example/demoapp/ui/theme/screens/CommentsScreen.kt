package com.example.demoapp.ui.theme.screens

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demoapp.data.model.CommentDto
import com.example.demoapp.ui.theme.viewmodel.CommentsViewModel
import com.example.demoapp.ui.theme.viewmodel.CommentsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    postId: Long,
    onBack: () -> Unit,
    vm: CommentsViewModel = viewModel(factory = CommentsViewModelFactory(postId))
) {
    val comments by vm.comments.collectAsState()
    val error by vm.error.collectAsState()
    var text by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadComments() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Комментарии") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Написать комментарий...") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            vm.createComment(text)
                            text = ""
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Отправить")
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (comments.isEmpty()) {
                Text(
                    "Комментариев пока нет",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(comments) { comment ->
                        CommentItem(comment = comment, onDelete = { vm.deleteComment(comment.id) })
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
}

@Composable
fun CommentItem(comment: CommentDto, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(comment.content) },
        supportingContent = { Text("User ${comment.userId}", style = MaterialTheme.typography.labelSmall) },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить")
            }
        }
    )
}