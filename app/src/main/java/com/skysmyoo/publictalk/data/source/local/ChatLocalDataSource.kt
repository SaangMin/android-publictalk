package com.skysmyoo.publictalk.data.source.local

import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import javax.inject.Inject

class ChatLocalDataSource @Inject constructor(
    private val chatModelDao: ChatModelDao,
    private val preferencesManager: SharedPreferencesManager
) {

    suspend fun getChatRoomList(): List<ChatRoom>? {
        return chatModelDao.getChatRoomList()
    }

    suspend fun insertChatRoom(chatRoom: ChatRoom) {
        chatModelDao.insertChatRoom(chatRoom)
    }

    suspend fun clearChatRooms() {
        chatModelDao.clearChatRooms()
    }
}