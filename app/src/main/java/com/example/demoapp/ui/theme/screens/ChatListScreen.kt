package com.example.demoapp.ui.theme.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demoapp.data.model.ChatConversation
import com.example.demoapp.ui.theme.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Список диалогов (как список чатов в Telegram).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    myEmail: String,
    onOpenChat: (chatId: String, otherEmail: String) -> Unit,
    vm: ChatViewModel = viewModel()
) {
    val conversations by vm.conversations.collectAsState()
    val error by vm.error.collectAsState()
    val ready by vm.ready.collectAsState()

    LaunchedEffect(Unit) {
        vm.ensureFirebaseSignIn()
    }
    LaunchedEffect(ready) {
        if (ready) vm.loadConversations()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Сообщения") }) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                !ready -> {
                    Column(
                        Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text("Подключение к чату…")
                    }
                }
                conversations.isEmpty() -> {
                    Text(
                        "Нет диалогов. Откройте профиль пользователя и напишите сообщение.",
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    LazyColumn {
                        items(conversations) { conv ->
                            ConversationItem(
                                conversation = conv,
                                myEmail = myEmail,
                                onClick = {
                                    val otherEmail = conv.participantEmails
                                        .firstOrNull { it != myEmail } ?: "Собеседник"
                                    onOpenChat(conv.id, otherEmail)
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
            error?.let {
                Snackbar(Modifier.align(Alignment.BottomCenter).padding(16.dp)) { Text(it) }
            }
        }
    }
}

@Composable
private fun ConversationItem(
    conversation: ChatConversation,
    myEmail: String,
    onClick: () -> Unit
) {
    val otherEmail = conversation.participantEmails.firstOrNull { it != myEmail } ?: "Собеседник"
    val time = if (conversation.lastTimestamp > 0)
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(conversation.lastTimestamp))
    else ""

    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(otherEmail, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(2.dp))
            Text(
                conversation.lastMessage.ifBlank { "Нет сообщений" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (time.isNotEmpty()) {
            Text(
                time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
