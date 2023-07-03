package com.skysmyoo.publictalk.data.source.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.skysmyoo.publictalk.BuildConfig
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.Message
import retrofit2.Response
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ChatRemoteDataSource @Inject constructor(private val apiClient: ApiClient) {

    suspend fun createChatRoom(auth: String, chatRoom: ChatRoom): Response<Map<String, String>> {
        return apiClient.createChatRoom(auth, chatRoom)
    }

    suspend fun getChatRooms(email: String): List<DataSnapshot>? {
        val ref = Firebase.database(BuildConfig.BASE_URL).getReference("chatRooms")
        return suspendCoroutine { continuation ->
            ref.orderByChild("me").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val chatRoomData = snapshot.children.toList()
                            continuation.resume(chatRoomData)
                        } else {
                            continuation.resume(null)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(null)
                    }
                })
        }
    }

    suspend fun sendMessage(
        chatRoomId: String,
        auth: String,
        message: Message
    ): Response<Map<String, String>> {
        return apiClient.sendMessage(chatRoomId, auth, message)
    }

    companion object {
        private const val TAG = "ChatRemoteDataSource"
    }
}