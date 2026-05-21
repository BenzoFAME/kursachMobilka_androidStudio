package com.example.demoapp.ui.theme.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.demoapp.ui.theme.viewmodel.ProfileViewModel
import com.example.demoapp.ui.theme.viewmodel.ProfileViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    email: String?,
    onBack: () -> Unit,
    vm: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(email))
) {
    val profile by vm.profile.collectAsState()
    val posts by vm.posts.collectAsState()
    val error by vm.error.collectAsState()
    val context = LocalContext.current

    val avatarLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { vm.uploadAvatar(it, context) } }

    LaunchedEffect(Unit) { vm.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (email == null) "Мой профиль" else "Профиль") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                profile?.let { p ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Аватарка
                            if (p.avatarUrl != null) {
                                AsyncImage(
                                    model = "http://10.0.2.2:8080${p.avatarUrl}",
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = p.username.take(2).uppercase(),
                                        color = Color.White,
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                            }

                            // Кнопка смены аватарки — только свой профиль
                            if (email == null) {
                                OutlinedButton(
                                    onClick = { avatarLauncher.launch("image/*") }
                                ) {
                                    Text("Сменить аватарку")
                                }
                            }

                            Text(
                                "${p.firstName} ${p.lastName}",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                "@${p.username}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                p.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } ?: CircularProgressIndicator(Modifier.padding(32.dp))
            }

            item {
                Text(
                    "Записи на стене",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (posts.isEmpty()) {
                item {
                    Text(
                        "Нет записей",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                items(posts) { post ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(post.content)

                            // Картинка поста если есть
                            if (post.imageUrl != null) {
                                Spacer(Modifier.height(4.dp))
                                AsyncImage(
                                    model = "http://10.0.2.2:8080${post.imageUrl}",
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Text(
                                post.createdAt,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            error?.let {
                item { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}