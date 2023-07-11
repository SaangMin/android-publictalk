package com.skysmyoo.publictalk.data.source.remote

import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.Message
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.remote.response.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiClient {

    @POST("users.json")
    suspend fun putUser(
        @Query("auth") auth: String,
        @Body user: User
    ): ApiResponse<Map<String, String>>

    @PUT("users/{uid}.json")
    suspend fun updateUser(
        @Path("uid") uid: String,
        @Query("auth") auth: String,
        @Body user: User
    ): ApiResponse<User>

    @POST("chatRooms.json")
    suspend fun createChatRoom(
        @Query("auth") auth: String,
        @Body chatRoom: ChatRoom
    ): ApiResponse<Map<String, String>>

    @POST("chatRooms/{chatRoomId}/messages.json")
    suspend fun sendMessage(
        @Path("chatRoomId") chatRoomId: String,
        @Query("auth") auth: String,
        @Body message: Message
    ): ApiResponse<Map<String, String>>

    @GET("chatRooms.json")
    suspend fun getChatRooms(
        @Query("auth") auth: String,
    ): ApiResponse<Map<String, ChatRoom>>
}