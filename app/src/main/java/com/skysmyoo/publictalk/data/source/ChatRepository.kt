package com.skysmyoo.publictalk.data.source

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.ChattingMember
import com.skysmyoo.publictalk.data.model.remote.Message
import com.skysmyoo.publictalk.data.source.local.ChatLocalDataSource
import com.skysmyoo.publictalk.data.source.remote.ChatRemoteDataSource
import com.skysmyoo.publictalk.utils.Constants
import com.skysmyoo.publictalk.utils.Constants.PATH_CHAT_ROOMS
import com.skysmyoo.publictalk.utils.Constants.PATH_IS_CHATTING
import com.skysmyoo.publictalk.utils.Constants.PATH_MEMBER
import com.skysmyoo.publictalk.utils.Constants.PATH_MESSAGES
import com.skysmyoo.publictalk.utils.Constants.PATH_USER_EMAIL
import com.skysmyoo.publictalk.utils.TimeUtil
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val localDataSource: ChatLocalDataSource,
    private val remoteDataSource: ChatRemoteDataSource,
) {

    private val chatRoomRef = FirebaseDatabase.getInstance().getReference(PATH_CHAT_ROOMS)

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
        val messagesRef = chatRoomRef.child(chatRoomKey).child(PATH_MESSAGES)
        val memberRef = chatRoomRef.child(chatRoomKey).child(PATH_MEMBER)

        memberRef.addChildEventListener(object : ChildEventListener {
            var isPartnerChatting = false

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val memberChatting =
                    snapshot.child(PATH_IS_CHATTING).getValue(Boolean::class.java)
                        ?: false
                val memberEmail =
                    snapshot.child(PATH_USER_EMAIL).getValue(String::class.java)
                if (memberEmail != myEmail) {
                    isPartnerChatting = memberChatting
                }

                if (isPartnerChatting) {
                    messagesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            snapshot.children.forEach { messageSnapshot ->
                                val message = messageSnapshot.getValue(Message::class.java)
                                if (message?.reading == false) {
                                    messageSnapshot.ref.child(Constants.PATH_READING).setValue(true)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "MessageDataSnapshotCancelled: $error")
                        }
                    })
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "MemberDataSnapshotCancelled: $error")
            }
        })
    }

    fun listenForChat(roomKey: String, receiveNewMessage: (Message) -> Unit) {
        val messageRef = chatRoomRef.child(roomKey).child(PATH_MESSAGES)

        messageRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val isMessageReading =
                    snapshot.child(Constants.PATH_READING).getValue(Boolean::class.java) ?: false
                val message = snapshot.getValue(Message::class.java) ?: return
                message.reading = isMessageReading
                if (message.sender.isEmpty() || message.receiver.isEmpty()) return
                receiveNewMessage(message)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "DataSnapshotCancelled: $error")
            }
        })
    }

    fun createNewMessage(chatRoom: ChatRoom, roomKey: String, messageBody: String, sendMessage: (Message) -> Unit) {
        val myEmail = localDataSource.getMyEmail()
        val otherEmail = chatRoom.member.map { it.userEmail }.find { it != myEmail } ?: ""
        val membersRef = chatRoomRef.child(roomKey).child(PATH_MEMBER)

        membersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isPartnerChatting = false
                snapshot.children.forEach { memberSnapshot ->
                    val memberName = memberSnapshot.child(PATH_USER_EMAIL).getValue(String::class.java)
                    if (memberName != myEmail) {
                        val memberChatting = memberSnapshot.child(PATH_IS_CHATTING).getValue(Boolean::class.java) ?: false
                        isPartnerChatting = memberChatting
                        return@forEach
                    }
                }
                val currentTime = TimeUtil.getCurrentDateString()
                val message = Message(
                    myEmail,
                    otherEmail,
                    messageBody,
                    isPartnerChatting,
                    currentTime
                )
                sendMessage(message)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "DataSnapshotCancelled: $error")
            }
        })
    }

    suspend fun getRoomKey(member: List<String>): String? {
        return remoteDataSource.getChatRoomKey(member)
    }

    companion object {
        private const val TAG = "ChatRepository"
    }
}