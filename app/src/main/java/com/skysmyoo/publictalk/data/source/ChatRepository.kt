package com.skysmyoo.publictalk.data.source

import com.google.firebase.database.FirebaseDatabase
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.ChattingMember
import com.skysmyoo.publictalk.data.model.remote.FcmNotification
import com.skysmyoo.publictalk.data.model.remote.Message
import com.skysmyoo.publictalk.data.source.local.ChatLocalDataSource
import com.skysmyoo.publictalk.data.source.remote.ChatRemoteDataSource
import com.skysmyoo.publictalk.data.source.remote.FcmDataSource
import com.skysmyoo.publictalk.data.source.remote.FirebaseData
import com.skysmyoo.publictalk.data.source.remote.TranslateDataSource
import com.skysmyoo.publictalk.data.source.remote.response.ApiResponse
import com.skysmyoo.publictalk.data.source.remote.response.ApiResultError
import com.skysmyoo.publictalk.data.source.remote.response.ApiResultException
import com.skysmyoo.publictalk.data.source.remote.response.ApiResultSuccess
import com.skysmyoo.publictalk.utils.Constants.PATH_CHAT_ROOMS
import com.skysmyoo.publictalk.utils.Constants.PATH_MESSAGES
import com.skysmyoo.publictalk.utils.TimeUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val localDataSource: ChatLocalDataSource,
    private val remoteDataSource: ChatRemoteDataSource,
    private val papagoDataSource: TranslateDataSource,
    private val fcmDataSource: FcmDataSource,
) {

    suspend fun deleteChatRoom(chatRoom: ChatRoom): ApiResponse<Map<String, String>> {
        val member = chatRoom.member.map { it.userEmail }.sorted()
        val authToken = FirebaseData.authToken ?: return ApiResultError(
            code = 400,
            message = "Not found firebase token"
        )
        return when (val roomKey = remoteDataSource.getChatRoomKey(member)) {
            is ApiResultSuccess -> {
                remoteDataSource.deleteChatRoom(authToken, roomKey.data)
            }

            else -> {
                ApiResultException(Throwable())
            }
        }
    }

    suspend fun translateText(targetLanguage: String, body: String): String {
        val sourceLanguage = localDataSource.getMyLocale()
        return when (val response =
            papagoDataSource.translateText(sourceLanguage, targetLanguage, body)) {
            is ApiResultSuccess -> {
                val translatedBody = response.data.message.result.translatedText
                translatedBody
            }

            else -> {
                body
            }
        }
    }

    suspend fun sendMessage(
        auth: String,
        message: Message,
        chatRoomId: String?
    ): ApiResponse<Message> {
        val database = FirebaseDatabase.getInstance()
        val currentTime = TimeUtil.getCurrentDateString()

        if (chatRoomId.isNullOrEmpty()) {
            val senderMember = ChattingMember(userEmail = message.sender)
            val receiverMember = ChattingMember(userEmail = message.receiver)
            val chattingMember = listOf(senderMember, receiverMember)
            val chatRoom = ChatRoom(
                member = chattingMember,
                chatCreatedAt = currentTime,
                messages = emptyMap()
            )
            val createRoomResponse = remoteDataSource.createChatRoom(auth, chatRoom)
            return if (createRoomResponse is ApiResultSuccess) {
                val chatRoomUid = createRoomResponse.data.values.first()
                val messagesRef =
                    database.getReference(PATH_CHAT_ROOMS).child(chatRoomUid).child(PATH_MESSAGES)
                messagesRef.push().setValue(message)
                ApiResultSuccess(message)
            } else {
                ApiResultError(code = 400, "Message send failed")
            }
        } else {
            val messagesRef =
                database.getReference(PATH_CHAT_ROOMS).child(chatRoomId).child(PATH_MESSAGES)
            messagesRef.push().setValue(message)
            return ApiResultSuccess(message)
        }
    }

    fun getMessages(chatRoom: ChatRoom): Flow<Message> {
        return flow {
            val member = chatRoom.member.map { it.userEmail }
            localDataSource.getChatRoomMessage(member).values.forEach {
                emit(it)
            }
            when (val roomKeyResponse = remoteDataSource.getChatRoomKey(member)) {
                is ApiResultSuccess -> {
                    val roomKey = roomKeyResponse.data
                    remoteDataSource.chatListenerFlow(roomKey).collect {
                        emit(it)
                    }
                }

                else -> localDataSource.getChatRoomMessage(member).values.forEach {
                    emit(it)
                }
            }
        }
    }

    fun enterChatting(roomKey: String, myIdKey: String) {
        if (roomKey.isEmpty()) return
        val email = localDataSource.getMyEmail()
        remoteDataSource.enterChatting(roomKey, myIdKey, email)
    }

    suspend fun createNewMessage(
        chatRoom: ChatRoom,
        roomKey: String,
        messageBody: String,
        sendMessage: (Message) -> Unit
    ) {
        val myEmail = localDataSource.getMyEmail()
        val otherEmail = chatRoom.member.map { it.userEmail }.find { it != myEmail } ?: ""

        remoteDataSource.memberStatusListenerFlow(myEmail, roomKey).collect {
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

    suspend fun sendNotification(
        serverKey: String,
        notification: FcmNotification
    ): ApiResponse<Unit> {
        return fcmDataSource.sendNotification(serverKey, notification)
    }

    companion object {
        private const val TAG = "ChatRepository"
    }
}