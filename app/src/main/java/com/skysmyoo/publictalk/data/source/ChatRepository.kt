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

    suspend fun getChatRooms(myEmail: String): List<ChatRoom?>? {
        val chatRoomDataList = remoteDataSource.getChatRooms(myEmail)
        return chatRoomDataList?.map { it.getValue(ChatRoom::class.java) }
    }

    suspend fun getCurrentRoom(myEmail: String, otherEmail: String): ChatRoom? {
        val chatRoomList = getChatRooms(myEmail)
        return chatRoomList?.find { (it?.me == myEmail && it.other?.userEmail == otherEmail) || it?.other?.userEmail == myEmail && it.me == otherEmail }
    }

    suspend fun sendMessage(
        auth: String,
        myEmail: String,
        other: User,
        message: Message
    ): Message? {
        val chatRoomDataList = remoteDataSource.getChatRooms(myEmail)
        val currentTime = TimeUtil.getCurrentDateString()
        val currentChatRoomData = chatRoomDataList?.find {
            it.getValue(ChatRoom::class.java)?.me == myEmail || it.getValue(ChatRoom::class.java)?.me == other.userEmail
        }
        if (currentChatRoomData == null) {
            val newChatRoom = ChatRoom(
                me = myEmail,
                other = other,
                messages = mapOf(),
                chatCreatedAt = currentTime
            )
            val createRoomResponse = remoteDataSource.createChatRoom(auth, newChatRoom)
            val chatRoomUid = createRoomResponse.body()?.values?.first()!!
            remoteDataSource.sendMessage(
                chatRoomUid,
                auth,
                message
            )
            return message
        } else {
            val chatRoomUid = currentChatRoomData.key ?: return null
            remoteDataSource.sendMessage(chatRoomUid, auth, message)
            return message
        }
    }

    companion object {
        private const val TAG = "ChatRepository"
    }
}