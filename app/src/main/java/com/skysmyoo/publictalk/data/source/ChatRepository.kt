package com.skysmyoo.publictalk.data.source

import com.google.firebase.database.FirebaseDatabase
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.ChattingMember
import com.skysmyoo.publictalk.data.model.remote.Message
import com.skysmyoo.publictalk.data.source.local.ChatLocalDataSource
import com.skysmyoo.publictalk.data.source.remote.ChatRemoteDataSource
import com.skysmyoo.publictalk.data.source.remote.response.ApiResultSuccess
import com.skysmyoo.publictalk.utils.Constants.PATH_CHAT_ROOMS
import com.skysmyoo.publictalk.utils.Constants.PATH_MESSAGES
import com.skysmyoo.publictalk.utils.TimeUtil
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val localDataSource: ChatLocalDataSource,
    private val remoteDataSource: ChatRemoteDataSource,
) {

    suspend fun sendMessage(auth: String, message: Message, chatRoomId: String?): Message? {
        val database = FirebaseDatabase.getInstance()
        val currentTime = TimeUtil.getCurrentDateString()

        if (chatRoomId == null) {
            val senderMember = ChattingMember(userEmail = message.sender)
            val receiverMember = ChattingMember(userEmail = message.receiver)
            val chattingMember = listOf(senderMember, receiverMember)
            val chatRoom = ChatRoom(member = chattingMember, chatCreatedAt = currentTime)
            val createRoomResponse = remoteDataSource.createChatRoom(auth, chatRoom)
            return if (createRoomResponse is ApiResultSuccess) {
                val chatRoomUid = createRoomResponse.data.values.first()
                val messagesRef =
                    database.getReference(PATH_CHAT_ROOMS).child(chatRoomUid).child(PATH_MESSAGES)
                messagesRef.push().setValue(message)
                message
            } else {
                null
            }
        } else {
            val messagesRef =
                database.getReference(PATH_CHAT_ROOMS).child(chatRoomId).child(PATH_MESSAGES)
            return if (messagesRef.push().setValue(message).isSuccessful) {
                message
            } else {
                null
            }
        }
    }

    suspend fun getChatRooms(auth: String, email: String): List<ChatRoom> {
        return when (val response = remoteDataSource.getChatRooms(auth)) {
            is ApiResultSuccess -> {
                val chatRooms = response.data.filterValues {
                    it.member.map { member -> member.userEmail }.contains(email)
                }
                chatRooms.values.toList()
            }

            else -> localDataSource.getChatRoomList() ?: emptyList()
        }


    }

    fun chatListener(roomKey: String, receiveNewMessage: (Message) -> Unit) {
        remoteDataSource.chatListener(roomKey, receiveNewMessage)
    }

    fun enterChatting(roomKey: String, myIdKey: String) {
        val email = localDataSource.getMyEmail()
        remoteDataSource.enterChatting(roomKey, myIdKey, email)
    }

    fun createNewMessage(
        chatRoom: ChatRoom,
        roomKey: String,
        messageBody: String,
        sendMessage: (Message) -> Unit
    ) {
        val myEmail = localDataSource.getMyEmail()
        val otherEmail = chatRoom.member.map { it.userEmail }.find { it != myEmail } ?: ""

        remoteDataSource.memberStatusListener(myEmail, roomKey) {
            val currentTime = TimeUtil.getCurrentDateString()
            val message = Message(
                myEmail,
                otherEmail,
                messageBody,
                it,
                currentTime
            )
            sendMessage(message)
        }
    }

    suspend fun getRoomKey(member: List<String>): String? {
        return when (val response = remoteDataSource.getChatRoomKey(member)) {
            is ApiResultSuccess -> {
                response.data
            }

            else -> null
        }
    }

    companion object {
        private const val TAG = "ChatRepository"
    }
}