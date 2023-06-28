package com.skysmyoo.publictalk.data.source.remote

import android.net.Uri
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.skysmyoo.publictalk.BuildConfig
import com.skysmyoo.publictalk.data.model.remote.Friend
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.utils.TimeUtil
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserRemoteDataSource @Inject constructor(private val apiClient: ApiClient) {
    suspend fun putUser(auth: String, user: User): Response<Map<String, String>> {
        return apiClient.putUser(auth, user)
    }

    suspend fun uploadImage(image: Uri?): String? {

        val currentTime = TimeUtil.getCurrentDateString()

        return if (image != null) {
            val location = "Profile images: $currentTime: $image"
            val imageRef = FirebaseData.storage.getReference(location)
            var downloadUri: Uri? = null
            imageRef.putFile(image)
                .await()
            imageRef.downloadUrl.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    downloadUri = task.result
                }
            }.await()
            downloadUri.toString()
        } else {
            null
        }
    }

    suspend fun getExistUser(email: String?): User? {
        val ref = Firebase.database(BuildConfig.BASE_URL).getReference("users")
        return suspendCoroutine { continuation ->
            ref.orderByChild("userEmail").equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val user =
                                snapshot.children.firstOrNull()?.getValue(User::class.java)
                                    ?: return
                            continuation.resume(user)
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

    suspend fun getFriends(friendList: List<Friend>): List<User?> =
        coroutineScope {
            friendList.map {
                getExistUser(it.userEmail)
            }
        }
}