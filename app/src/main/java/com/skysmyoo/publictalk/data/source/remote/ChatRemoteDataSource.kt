package com.skysmyoo.publictalk.data.source.remote

import android.util.Log
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.Message
import com.skysmyoo.publictalk.utils.Constants
import com.skysmyoo.publictalk.utils.Constants.PATH_CHAT_ROOMS
import retrofit2.Response
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ChatRemoteDataSource @Inject constructor(private val apiClient: ApiClient) {

    private val chatRoomRef = FirebaseDatabase.getInstance().getReference(PATH_CHAT_ROOMS)

    suspend fun createChatRoom(auth: String, chatRoom: ChatRoom): Response<Map<String, String>> {
        return apiClient.createChatRoom(auth, chatRoom)
    }

    suspend fun getChatRooms(auth: String, email: String): List<ChatRoom> {
        val response = apiClient.getChatRooms(auth)
        val chatRooms = response.body()
            ?.filterValues { it.member.map { member -> member.userEmail }.contains(email) }
        return chatRooms?.values?.toList() ?: emptyList()
    }

    suspend fun getChatRoomKey(member: List<String>): String? = suspendCoroutine { continuation ->
        chatRoomRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatRoomId = snapshot.children.find {
                    val chatRoom = it.getValue(ChatRoom::class.java)
                    val chattingMember = chatRoom?.member?.map { member -> member.userEmail }
                    chattingMember?.contains(member[0]) == true && chattingMember.contains(member[1])
                }?.key ?: return

                continuation.resume(chatRoomId)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "DataSnapshotCancelled: $error")
                continuation.resume(null)
            }
        })
    }

    fun updateMemberListener(myEmail: String, chatRoomKey: String) {
        val messagesRef = chatRoomRef.child(chatRoomKey).child(Constants.PATH_MESSAGES)
        val memberRef = chatRoomRef.child(chatRoomKey).child(Constants.PATH_MEMBER)

        memberRef.addChildEventListener(object : ChildEventListener {
            var isPartnerChatting = false

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val memberChatting =
                    snapshot.child(Constants.PATH_IS_CHATTING).getValue(Boolean::class.java)
                        ?: false
                val memberEmail =
                    snapshot.child(Constants.PATH_USER_EMAIL).getValue(String::class.java)
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

    fun chatListener(roomKey: String, receiveNewMessage: (Message) -> Unit) {
        val messageRef = chatRoomRef.child(roomKey).child(Constants.PATH_MESSAGES)

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

    fun memberStatusListener(
        myEmail: String,
        roomKey: String,
        memberStatusMethod: (Boolean) -> Unit
    ) {
        val membersRef = chatRoomRef.child(roomKey).child(Constants.PATH_MEMBER)

        membersRef.addListenerForSingleValueEvent(object : ValueEventListener {
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
                memberStatusMethod(isPartnerChatting)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "DataSnapshotCancelled: $error")
            }
        })
    }

    companion object {
        private const val TAG = "ChatRemoteDataSource"
    }
}