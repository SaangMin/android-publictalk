package com.skysmyoo.publictalk.data.source.remote

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
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

        val query1 = ref.orderByChild("me").equalTo(email)
        val query2 = ref.orderByChild("other/userEmail").equalTo(email)

        val results1 = performQuery(query1)
        val results2 = performQuery(query2)

        return results1?.plus(results2 ?: emptyList()) ?: results2
    }

    private suspend fun performQuery(query: Query): List<DataSnapshot>? {
        return suspendCoroutine { continuation ->
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        continuation.resume(snapshot.children.toList())
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