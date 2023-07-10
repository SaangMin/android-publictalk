package com.skysmyoo.publictalk.data.source

import com.google.firebase.database.FirebaseDatabase
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.ChattingMember
import com.skysmyoo.publictalk.data.model.remote.Message
import com.skysmyoo.publictalk.data.source.local.ChatLocalDataSource
import com.skysmyoo.publictalk.data.source.remote.ChatRemoteDataSource
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
            val chatRoomUid = createRoomResponse.body()?.values?.first() ?: return null
            val messagesRef =
                database.getReference(PATH_CHAT_ROOMS).child(chatRoomUid).child(PATH_MESSAGES)
            messagesRef.push().setValue(message)
            return message
        } else {
            val messagesRef =
                database.getReference(PATH_CHAT_ROOMS).child(chatRoomId).child(PATH_MESSAGES)
            messagesRef.push().setValue(message)
            return message
        }
    }

    fun updateIsReadingForMessages(chatRoomKey: String) {
        val myEmail = localDataSource.getMyEmail()
        remoteDataSource.updateMemberListener(myEmail, chatRoomKey)
    }

    fun chatListener(roomKey: String, receiveNewMessage: (Message) -> Unit) {
        remoteDataSource.chatListener(roomKey, receiveNewMessage)
    }

    fun createNewMessage(chatRoom: ChatRoom, roomKey: String, messageBody: String, sendMessage: (Message) -> Unit) {
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
        return remoteDataSource.getChatRoomKey(member)
    }

    companion object {
        private const val TAG = "ChatRepository"
    }
}