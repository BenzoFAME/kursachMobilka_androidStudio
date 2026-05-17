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
import com.example.demoapp.data.model.ChannelDto
import com.example.demoapp.ui.theme.viewmodel.ChannelsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelsScreen(
    onOpenChannel: (Long) -> Unit,
    vm: ChannelsViewModel = viewModel()
) {
    val channels by vm.channels.collectAsState()
    val error by vm.error.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.loadChannels() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Каналы") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Создать канал")
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (channels.isEmpty()) {
                Text(
                    "Нет каналов. Создайте первый!",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn {
                    items(channels) { channel ->
                        ChannelItem(
                            channel = channel,
                            onClick = { onOpenChannel(channel.id) },
                            onSubscribe = { vm.subscribe(channel.id) },
                            onDelete = { vm.deleteChannel(channel.id) }
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
        CreateChannelDialog(
            onDismiss = { showDialog = false },
            onCreate = { name, desc ->
                vm.createChannel(name, desc)
                showDialog = false
            }
        )
    }
}

@Composable
fun ChannelItem(
    channel: ChannelDto,
    onClick: () -> Unit,
    onSubscribe: () -> Unit,
    onDelete: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(channel.name) },
        supportingContent = { Text(channel.description, maxLines = 2) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${channel.subscribersCount}", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onSubscribe) { Text("Sub") }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить")
                }
            }
        }
    )
}

@Composable
fun CreateChannelDialog(onDismiss: () -> Unit, onCreate: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новый канал") },
        text = {
            Column {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Название") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc, onValueChange = { desc = it },
                    label = { Text("Описание") }, modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onCreate(name, desc) }) { Text("Создать") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}