package com.example.demoapp.data.chat

import com.example.demoapp.data.api.RetrofitClient
import com.example.demoapp.data.model.ChatConversation
import com.example.demoapp.data.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Репозиторий чата на Firebase.
 *
 * Авторизация: берём Firebase custom token у нашего Spring-бэкенда (/firebase/token),
 * затем входим в Firebase Auth через signInWithCustomToken. После этого uid пользователя
 * в Firebase = "uid_<id>" из нашей БД, и Firestore rules могут проверять личность.
 *
 * Хранилище: Firestore.
 *   chats/{chatId}                 — документ диалога (участники, последнее сообщение)
 *   chats/{chatId}/messages/{id}   — сообщения диалога
 */
class ChatRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val currentUid: String? get() = auth.currentUser?.uid

    /** Входим в Firebase Auth по custom token от нашего бэкенда. Вызывать после логина. */
    suspend fun signInToFirebase(): Result<String> = try {
        if (auth.currentUser != null) {
            Result.success(auth.currentUser!!.uid)
        } else {
            val response = RetrofitClient.api.getFirebaseToken()
            val token = response.body()?.get("firebaseToken")
                ?: return Result.failure(Exception("Бэкенд не вернул firebaseToken (код ${response.code()})"))
            val result = auth.signInWithCustomToken(token).await()
            Result.success(result.user!!.uid)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun signOut() = auth.signOut()

    /** Детерминированный id диалога из двух uid — одинаковый у обоих собеседников. */
    private fun chatIdFor(uidA: String, uidB: String): String =
        listOf(uidA, uidB).sorted().joinToString("_")

    /** Поток списка диалогов текущего пользователя (обновляется в реальном времени). */
    fun observeConversations(): Flow<List<ChatConversation>> = callbackFlow {
        val uid = currentUid
        if (uid == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val registration = db.collection("chats")
            .whereArrayContains("participants", uid)
            .orderBy("lastTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatConversation::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    /** Поток сообщений конкретного диалога (обновляется в реальном времени). */
    fun observeMessages(chatId: String): Flow<List<ChatMessage>> = callbackFlow {
        val registration = db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    /**
     * Открыть (или создать) диалог с собеседником по его uid и email.
     * Возвращает chatId.
     */
    suspend fun openConversation(otherUid: String, otherEmail: String, myEmail: String): String {
        val uid = currentUid ?: throw IllegalStateException("Не выполнен вход в Firebase")
        val chatId = chatIdFor(uid, otherUid)
        val docRef = db.collection("chats").document(chatId)
        val snapshot = docRef.get().await()
        if (!snapshot.exists()) {
            val conversation = ChatConversation(
                id = chatId,
                participants = listOf(uid, otherUid).sorted(),
                participantEmails = listOf(myEmail, otherEmail),
                lastMessage = "",
                lastTimestamp = System.currentTimeMillis()
            )
            docRef.set(conversation).await()
        }
        return chatId
    }

    /**
     * Открыть диалог, зная только email собеседника.
     * Сначала спрашиваем у бэкенда uid этого пользователя (profile/lookup), затем создаём диалог.
     */
    suspend fun openConversationByEmail(otherEmail: String, myEmail: String): String {
        val response = RetrofitClient.api.lookupUser(otherEmail)
        val otherUid = response.body()?.get("uid")
            ?: throw IllegalStateException("Не удалось найти пользователя $otherEmail (код ${response.code()})")
        return openConversation(otherUid, otherEmail, myEmail)
    }

    /** Отправить сообщение в диалог. */
    suspend fun sendMessage(chatId: String, text: String, myEmail: String) {
        val uid = currentUid ?: throw IllegalStateException("Не выполнен вход в Firebase")
        val now = System.currentTimeMillis()
        val message = ChatMessage(
            senderUid = uid,
            senderEmail = myEmail,
            text = text,
            timestamp = now
        )
        val chatRef = db.collection("chats").document(chatId)
        // добавляем сообщение
        chatRef.collection("messages").add(message).await()
        // обновляем превью последнего сообщения в документе диалога
        chatRef.update(
            mapOf(
                "lastMessage" to text,
                "lastTimestamp" to now
            )
        ).await()
    }
}
