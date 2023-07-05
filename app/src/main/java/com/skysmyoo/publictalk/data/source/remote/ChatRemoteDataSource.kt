package com.skysmyoo.publictalk.data.source.remote

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import retrofit2.Response
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ChatRemoteDataSource @Inject constructor(private val apiClient: ApiClient) {

    suspend fun createChatRoom(auth: String, chatRoom: ChatRoom): Response<Map<String, String>> {
        return apiClient.createChatRoom(auth, chatRoom)
    }

    suspend fun getChatRooms(auth: String, email: String): List<ChatRoom> {
        val response = apiClient.getChatRooms(auth)
        val chatRooms = response.body()?.filterValues { it.member.contains(email) }
        return chatRooms?.values?.toList() ?: emptyList()
    }

    suspend fun getChatRoomKey(member: List<String>): String? = suspendCoroutine { continuation ->
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("chatRooms")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatRoomId = snapshot.children.find {
                    val chatRoom = it.getValue(ChatRoom::class.java)
                    chatRoom?.member?.contains(member[0]) == true && chatRoom.member.contains(member[1])
                }?.key ?: return

                continuation.resume(chatRoomId)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "DataSnapshotCancelled: $error")
                continuation.resume(null)
            }
        })
    }

    companion object {
        private const val TAG = "ChatRemoteDataSource"
    }
}