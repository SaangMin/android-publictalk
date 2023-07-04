package com.skysmyoo.publictalk.data.source

import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.Message
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.local.ChatLocalDataSource
import com.skysmyoo.publictalk.data.source.remote.ChatRemoteDataSource
import com.skysmyoo.publictalk.utils.TimeUtil
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val localDataSource: ChatLocalDataSource,
    private val remoteDataSource: ChatRemoteDataSource,
) {

    suspend fun sendMessage(
        auth: String,
        myEmail: String,
        other: User,
        message: Message
    ): List<Message>? {
        val chatRoomDataList = remoteDataSource.getChatRooms(myEmail)
        val currentTime = TimeUtil.getCurrentDateString()
        val currentChatRoomData = chatRoomDataList?.firstOrNull()
        if (currentChatRoomData == null) {
            val newChatRoom = ChatRoom(
                me = myEmail,
                other = other,
                messages = listOf(),
                chatCreatedAt = currentTime
            )
            val createRoomResponse = remoteDataSource.createChatRoom(auth, newChatRoom)
            remoteDataSource.sendMessage(
                createRoomResponse.body()?.values?.first()!!,
                auth,
                message
            )
            return newChatRoom.messages.map { it.values.first() }
        } else {
            val chatRoomUid = currentChatRoomData.key ?: return null
            val responseSendMessage = remoteDataSource.sendMessage(chatRoomUid, auth, message)
            return null
        }
    }

    companion object {
        private const val TAG = "ChatRepository"
    }
}