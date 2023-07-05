package com.skysmyoo.publictalk.data.source.remote

import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.Message
import retrofit2.Response
import javax.inject.Inject

class ChatRemoteDataSource @Inject constructor(private val apiClient: ApiClient) {

    suspend fun createChatRoom(auth: String, chatRoom: ChatRoom): Response<Map<String, String>> {
        return apiClient.createChatRoom(auth, chatRoom)
    }

    suspend fun getChatRooms(auth: String, email: String): List<ChatRoom> {
        val response = apiClient.getChatRooms(auth)
        val chatRooms = response.body()?.filterValues { it.member.contains(email) }
        return chatRooms?.values?.toList() ?: emptyList()
    }

    suspend fun sendMessage(
        chatRoomId: String,
        auth: String,
        message: Message
    ): Response<Map<String, String>> {
        return apiClient.sendMessage(chatRoomId, auth, message)
    }

    companion object {
        private const val TAG = "ChatRemoteDataSource"
    }
}