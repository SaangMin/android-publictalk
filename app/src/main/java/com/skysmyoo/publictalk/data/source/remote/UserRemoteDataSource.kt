package com.skysmyoo.publictalk.data.source.remote

import android.net.Uri
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.skysmyoo.publictalk.BuildConfig
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.utils.Constants.PATH_USER_EMAIL
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

    suspend fun getExistUser(email: String?): DataSnapshot? {
        val ref = Firebase.database(BuildConfig.BASE_URL).getReference("users")
        return suspendCoroutine { continuation ->
            ref.orderByChild(PATH_USER_EMAIL).equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val userData = snapshot.children.firstOrNull() ?: return
                            continuation.resume(userData)
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

    suspend fun updateUser(auth: String, user: User): Response<User>? {
        val userEmail = user.userEmail
        val userUid = getExistUser(userEmail)?.key ?: return null
        user.userDeviceToken = FirebaseData.token ?: return null
        return apiClient.updateUser(userUid, auth, user)
    }

    suspend fun updateFriendsData(friendList: List<String>): List<User?> =
        coroutineScope {
            friendList.map {
                val dataSnapshot = getExistUser(it)
                val updatedUser = dataSnapshot?.getValue(User::class.java)
                updatedUser
            }
        }
}