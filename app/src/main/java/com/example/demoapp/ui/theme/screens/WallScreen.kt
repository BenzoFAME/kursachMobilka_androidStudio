package com.example.demoapp.ui.theme.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
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
    val context = LocalContext.current

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
            onCreate = { content, imageUri ->
                vm.createPost(content, imageUri, context)
                showDialog = false
            }
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenComments() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "@${post.authorUsername}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onOpenProfile() }
                )
                Text(post.createdAt, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(post.content, style = MaterialTheme.typography.bodyMedium, maxLines = 5)

        // Картинка поста
        if (post.imageUrl != null) {
            Spacer(Modifier.height(8.dp))
            AsyncImage(
                model = "http://10.0.2.2:8080${post.imageUrl}",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }

        if (post.commentDto.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                "${post.commentDto.size} комм.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CreateWallPostDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Uri?) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая запись") },
        text = {
            Column {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Что у вас нового?") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(Modifier.height(8.dp))

                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(4.dp))
                }

                OutlinedButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (imageUri == null) "Добавить фото" else "Изменить фото")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (content.isNotBlank()) onCreate(content, imageUri)
            }) { Text("Опубликовать") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}