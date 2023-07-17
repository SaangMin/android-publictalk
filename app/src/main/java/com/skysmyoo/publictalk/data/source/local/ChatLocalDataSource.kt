package com.skysmyoo.publictalk.data.source.local

import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.Message
import javax.inject.Inject

class ChatLocalDataSource @Inject constructor(
    private val chatModelDao: ChatModelDao,
    private val preferencesManager: SharedPreferencesManager
) {

    fun getMyEmail(): String {
        return preferencesManager.getMyEmail() ?: ""
    }

    suspend fun getChatRoomList(): List<ChatRoom>? {
        return chatModelDao.getChatRoomList()
    }

    suspend fun insertChatRoom(chatRoom: ChatRoom) {
        chatModelDao.insertChatRoom(chatRoom)
    }

    suspend fun clearChatRooms() {
        chatModelDao.clearChatRooms()
    }

    suspend fun getChatRoomMessage(member: List<String>): Map<String, Message> {
        val chatRoomList = chatModelDao.getChatRoomList() ?: return emptyMap()
        val sortedMember = member.sorted()
        val chatRoom = chatRoomList.find {
            it.member.map { chattingMember -> chattingMember.userEmail }.sorted() == sortedMember
        }
        return chatRoom?.messages ?: emptyMap()
    }
}