package com.example.demoapp.data.api
import com.example.demoapp.data.model.ChannelDto
import com.example.demoapp.data.model.CommentDto
import com.example.demoapp.data.model.CreateChannelRequest
import com.example.demoapp.data.model.CreateCommentRequest
import com.example.demoapp.data.model.CreatePostRequest
import com.example.demoapp.data.model.CreateWallPostRequest
import com.example.demoapp.data.model.JwtResponse
import com.example.demoapp.data.model.LoginRequest
import com.example.demoapp.data.model.PostDto
import com.example.demoapp.data.model.ProfileDto
import com.example.demoapp.data.model.RefreshRequest
import com.example.demoapp.data.model.RegisterRequest
import com.example.demoapp.data.model.WallPostDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<JwtResponse>

    // Firebase custom token (для входа в Firebase Auth и работы чата)
    @GET("firebase/token")
    suspend fun getFirebaseToken(): Response<Map<String, String>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<JwtResponse>

    // Обновление access-токена по refresh-токену
    @POST("auth/refreshToken")
    suspend fun refreshToken(@Body request: RefreshRequest): Response<JwtResponse>

    // Channels
    @GET("channels")
    suspend fun getChannels(): Response<List<ChannelDto>>

    @POST("channels/create")
    suspend fun createChannel(@Body request: CreateChannelRequest): Response<ChannelDto>

    @DELETE("channels/{id}")
    suspend fun deleteChannel(@Path("id") id: Long): Response<Void>

    @POST("channels/subscribe/{id}")
    suspend fun subscribe(@Path("id") id: Long): Response<ChannelDto>

    @POST("channels/unsubscribe/{id}")
    suspend fun unsubscribe(@Path("id") id: Long): Response<ChannelDto>

    // Posts
    @GET("api/channels/{channelId}/posts")
    suspend fun getPosts(@Path("channelId") channelId: Long): Response<List<PostDto>>

    @POST("api/channels/{channelId}/posts")
    suspend fun createPost(
        @Path("channelId") channelId: Long,
        @Body request: CreatePostRequest
    ): Response<PostDto>

    @DELETE("api/channels/{channelId}/posts/{id}")
    suspend fun deletePost(
        @Path("channelId") channelId: Long,
        @Path("id") id: Long
    ): Response<Void>

    // Comments (channel posts)
    @GET("api/posts/{postId}/comments")
    suspend fun getComments(@Path("postId") postId: Long): Response<List<CommentDto>>

    @POST("api/posts/{postId}/comments")
    suspend fun createComment(
        @Path("postId") postId: Long,
        @Body request: CreateCommentRequest
    ): Response<CommentDto>

    @DELETE("api/posts/{postId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("postId") postId: Long,
        @Path("commentId") commentId: Long
    ): Response<Void>

    // Wall
    @GET("wall")
    suspend fun getWallPosts(): Response<List<WallPostDto>>

    @POST("wall")
    suspend fun createWallPost(@Body request: CreateWallPostRequest): Response<WallPostDto>

    @DELETE("wall/{id}")
    suspend fun deleteWallPost(@Path("id") id: Long): Response<Void>

    // Wall comments
    @GET("wall/{postId}/comments")
    suspend fun getWallComments(@Path("postId") postId: Long): Response<List<CommentDto>>

    @POST("wall/{postId}/comments")
    suspend fun createWallComment(
        @Path("postId") postId: Long,
        @Body request: CreateCommentRequest
    ): Response<CommentDto>

    @DELETE("wall/{postId}/comments/{commentId}")
    suspend fun deleteWallComment(
        @Path("postId") postId: Long,
        @Path("commentId") commentId: Long
    ): Response<Void>

    // Profile
    @GET("profile")
    suspend fun getMyProfile(): Response<ProfileDto>

    // Полный профиль другого пользователя по email
    @GET("profile/user/{email}")
    suspend fun getUserProfile(@Path("email") email: String): Response<ProfileDto>

    // Lookup пользователя по email (для начала чата — возвращает uid)
    @GET("profile/lookup/{email}")
    suspend fun lookupUser(@Path("email") email: String): Response<Map<String, String>>

    @GET("wall/user/{email}")
    suspend fun getWallPostsByEmail(@Path("email") email: String): Response<List<WallPostDto>>

    @Multipart
    @POST("files/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): Response<Map<String, String>>

    @Multipart
    @POST("profile/avatar")
    suspend fun uploadAvatar(
        @Part file: MultipartBody.Part
    ): Response<Map<String, String>>
}