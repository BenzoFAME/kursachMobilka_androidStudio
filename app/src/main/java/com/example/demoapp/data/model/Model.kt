package com.example.demoapp.data.model

data class RegisterRequest(
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class JwtResponse(
    val accessToken: String,
    val refreshToken: String
)

// Тело запроса на обновление токена
data class RefreshRequest(
    val refreshToken: String
)

data class ChannelDto(
    val id: Long,
    val name: String,
    val description: String,
    val ownerEmail: String,
    val subscribersCount: Int
)

data class CreateChannelRequest(
    val name: String,
    val description: String
)

data class PostDto(
    val id: Long,
    val content: String,
    val createdAt: String,
    val channelName: String,
    val commentDto: List<CommentDto> = emptyList()
)

data class CreatePostRequest(
    val content: String
)
data class CommentDto(
    val id: Long,
    val content: String,
    val userId: Long,        // ← было authorEmail: String
    val created_at: String,
    val updatedAt: String
)

data class CreateCommentRequest(
    val content: String
)
data class WallPostDto(
    val id: Long,
    val content: String,
    val createdAt: String,
    val authorEmail: String,
    val authorUsername: String,
    val commentDto: List<CommentDto> = emptyList(),
    val imageUrl: String? = null
)

data class CreateWallPostRequest(
    val content: String,
    val imageUrl: String? = null
)
data class ProfileDto(
    val id: Long = 0,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val createdAt: String,
    val posts: List<WallPostDto> = emptyList(),
    val avatarUrl: String? = null
)