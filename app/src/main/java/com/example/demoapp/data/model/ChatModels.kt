package com.example.demoapp.data.model

/**
 * Модель сообщения в чате (хранится в Firestore).
 * Путь: chats/{chatId}/messages/{messageId}
 */
data class ChatMessage(
    val id: String = "",
    val senderUid: String = "",
    val senderEmail: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)

/**
 * Модель диалога (хранится в Firestore).
 * Путь: chats/{chatId}
 *
 * chatId формируется детерминированно из двух uid (сортируются и склеиваются),
 * чтобы у обоих собеседников был один и тот же документ диалога.
 */
data class ChatConversation(
    val id: String = "",
    val participants: List<String> = emptyList(),       // [uid1, uid2]
    val participantEmails: List<String> = emptyList(),  // [email1, email2]
    val lastMessage: String = "",
    val lastTimestamp: Long = 0L
)
