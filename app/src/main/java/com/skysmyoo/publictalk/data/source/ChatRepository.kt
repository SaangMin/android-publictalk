package com.skysmyoo.publictalk.data.source

import com.google.firebase.database.FirebaseDatabase
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.Message
import com.skysmyoo.publictalk.data.source.local.ChatLocalDataSource
import com.skysmyoo.publictalk.data.source.remote.ChatRemoteDataSource
import com.skysmyoo.publictalk.utils.TimeUtil
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val localDataSource: ChatLocalDataSource,
    private val remoteDataSource: ChatRemoteDataSource,
) {

    suspend fun sendMessage(auth: String, message: Message): Message? {
        val database = FirebaseDatabase.getInstance()
        val member = listOf(message.sender, message.receiver)
        val chatRoomId = remoteDataSource.getChatRoomKey(member)
        val currentTime = TimeUtil.getCurrentDateString()

        if (chatRoomId == null) {
            val chatRoom = ChatRoom(member = member, chatCreatedAt = currentTime)
            val createRoomResponse = remoteDataSource.createChatRoom(auth, chatRoom)
            val chatRoomUid = createRoomResponse.body()?.values?.first() ?: return null
            val messagesRef = database.getReference("chatRooms/$chatRoomUid/messages")
            messagesRef.push().setValue(message)
            return message
        } else {
            val messagesRef = database.getReference("chatRooms/$chatRoomId/messages")
            messagesRef.push().setValue(message)
            return message
        }
    }

    suspend fun getRoomKey(member: List<String>): String? {
        return remoteDataSource.getChatRoomKey(member)
    }

    companion object {
        private const val TAG = "ChatRepository"
    }
}