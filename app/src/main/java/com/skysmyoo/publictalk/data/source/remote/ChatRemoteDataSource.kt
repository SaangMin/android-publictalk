package com.skysmyoo.publictalk.data.source.remote

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.Message
import com.skysmyoo.publictalk.data.source.remote.response.ApiResponse
import com.skysmyoo.publictalk.data.source.remote.response.ApiResultError
import com.skysmyoo.publictalk.data.source.remote.response.ApiResultSuccess
import com.skysmyoo.publictalk.utils.Constants
import com.skysmyoo.publictalk.utils.Constants.PATH_CHAT_ROOMS
import com.skysmyoo.publictalk.utils.Constants.PATH_IS_CHATTING
import com.skysmyoo.publictalk.utils.Constants.PATH_MEMBER
import com.skysmyoo.publictalk.utils.Constants.PATH_READING
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ChatRemoteDataSource @Inject constructor(private val apiClient: ApiClient) {

    private val chatRoomRef = FirebaseDatabase.getInstance().getReference(PATH_CHAT_ROOMS)

    suspend fun createChatRoom(auth: String, chatRoom: ChatRoom): ApiResponse<Map<String, String>> {
        return apiClient.createChatRoom(auth, chatRoom)
    }

    suspend fun getChatRooms(auth: String): ApiResponse<Map<String, ChatRoom>> {
        return apiClient.getChatRooms(auth)
    }

    suspend fun deleteChatRoom(auth: String, chatRoomId: String): ApiResponse<Map<String, String>> {
        return apiClient.deleteChatRoom(chatRoomId, auth)
    }

    suspend fun getChatRoomKey(member: List<String>): ApiResponse<String> =
        suspendCoroutine { continuation ->
            chatRoomRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chatRoomId = snapshot.children.find {
                        val chatRoom = it.getValue(ChatRoom::class.java)
                        val chattingMember = chatRoom?.member?.map { member -> member.userEmail }
                        chattingMember?.contains(member[0]) == true && chattingMember.contains(
                            member[1]
                        )
                    }?.key ?: return

                    continuation.resume(ApiResultSuccess(chatRoomId))
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(ApiResultError(code = 500, "DatabaseError: $error"))
                }
            })
        }

    fun enterChatting(roomKey: String, myIdKey: String, myEmail: String) {
        val messageRef = chatRoomRef.child(roomKey).child(Constants.PATH_MESSAGES)
        val myInfoRef =
            chatRoomRef.child(roomKey).child(PATH_MEMBER).child(myIdKey)

        myInfoRef.child(PATH_IS_CHATTING).setValue(true).addOnSuccessListener {
            messageRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { messageSnapshot ->
                        val message = messageSnapshot.getValue(Message::class.java)
                        if (message?.receiver == myEmail && !message.reading) {
                            messageSnapshot.ref.child(PATH_READING).setValue(true)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "MessageDataSnapshotCancelled: $error")
                }
            })
        }
        myInfoRef.child(PATH_IS_CHATTING).onDisconnect().removeValue()
    }

    fun chatListenerFlow(roomKey: String): Flow<Message> = callbackFlow {
        val messageRef = chatRoomRef.child(roomKey).child(Constants.PATH_MESSAGES)

        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val isMessageReading =
                    snapshot.child(Constants.PATH_READING).getValue(Boolean::class.java) ?: false
                val message = snapshot.getValue(Message::class.java) ?: return
                message.reading = isMessageReading
                if (message.sender.isEmpty() || message.receiver.isEmpty()) return
                trySend(message).isSuccess
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "DataSnapshotCancelled: $error")
            }
        }
        messageRef.addChildEventListener(childEventListener)
        awaitClose { messageRef.removeEventListener(childEventListener) }
    }

    fun memberStatusListenerFlow(
        myEmail: String,
        roomKey: String
    ): Flow<Boolean> = callbackFlow {
        val membersRef = chatRoomRef.child(roomKey).child(Constants.PATH_MEMBER)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isPartnerChatting = false
                snapshot.children.forEach { memberSnapshot ->
                    val memberName =
                        memberSnapshot.child(Constants.PATH_USER_EMAIL).getValue(String::class.java)
                    if (memberName != myEmail) {
                        val memberChatting = memberSnapshot.child(Constants.PATH_IS_CHATTING)
                            .getValue(Boolean::class.java) ?: false
                        isPartnerChatting = memberChatting
                        return@forEach
                    }
                }
                trySend(isPartnerChatting).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "DataSnapshotCancelled: $error")
            }
        }
        membersRef.addListenerForSingleValueEvent(valueEventListener)
        awaitClose { membersRef.removeEventListener(valueEventListener) }
    }

    companion object {
        private const val TAG = "ChatRemoteDataSource"
    }
}