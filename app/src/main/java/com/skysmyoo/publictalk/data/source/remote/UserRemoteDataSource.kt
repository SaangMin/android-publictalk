package com.skysmyoo.publictalk.data.source.remote

import android.net.Uri
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.skysmyoo.publictalk.BuildConfig
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.remote.response.ApiResponse
import com.skysmyoo.publictalk.data.source.remote.response.ApiResultError
import com.skysmyoo.publictalk.data.source.remote.response.ApiResultException
import com.skysmyoo.publictalk.data.source.remote.response.ApiResultSuccess
import com.skysmyoo.publictalk.utils.Constants.PATH_USERS
import com.skysmyoo.publictalk.utils.Constants.PATH_USER_EMAIL
import com.skysmyoo.publictalk.utils.Constants.PATH_USER_FRIEND_ID_LIST
import com.skysmyoo.publictalk.utils.TimeUtil
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserRemoteDataSource @Inject constructor(private val apiClient: ApiClient) {

    private val userRef = Firebase.database(BuildConfig.BASE_URL).getReference(PATH_USERS)

    suspend fun putUser(auth: String, user: User): ApiResponse<Map<String, String>> {
        return apiClient.putUser(auth, user)
    }

    suspend fun uploadImage(image: Uri?): ApiResponse<String>? {

        val currentTime = TimeUtil.getCurrentDateString()

        return if (image != null) {
            val location = "Profile images: $currentTime: $image"
            val imageRef = FirebaseData.storage.getReference(location)
            var downloadUri: Uri? = null
            imageRef.putFile(image).await()
            imageRef.downloadUrl.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    downloadUri = task.result
                } else {
                    ApiResultError<String>(code = 500, "Task Fail!")
                }
            }.await()
            ApiResultSuccess(downloadUri.toString())
        } else {
            null
        }
    }

    suspend fun getExistUser(email: String?): ApiResponse<DataSnapshot>? =
        suspendCoroutine { continuation ->
            userRef.orderByChild(PATH_USER_EMAIL).equalTo(email)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val userData = snapshot.children.firstOrNull() ?: return
                            continuation.resume(ApiResultSuccess(userData))
                        } else {
                            continuation.resume(null)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(
                            ApiResultError(
                                code = 500,
                                message = "Database error: $error"
                            )
                        )
                    }
                })
        }

    suspend fun updateUser(auth: String, user: User): ApiResponse<User> {
        val userEmail = user.userEmail
        val userUid = getExistUser(userEmail)
        if (userUid is ApiResultSuccess) {
            val key = userUid.data.key ?: return ApiResultError(code = 400, "Doesn't have key.")
            return apiClient.updateUser(key, auth, user)
        } else {
            return ApiResultException(Throwable())
        }
    }

    suspend fun updateFriendsData(friendList: List<String>): ApiResponse<List<User?>> =
        coroutineScope {
            ApiResultSuccess(friendList.map {
                val dataSnapshot = getExistUser(it)
                if (dataSnapshot is ApiResultSuccess) {
                    val updatedUser = dataSnapshot.data.getValue(User::class.java)
                    updatedUser
                } else {
                    return@coroutineScope ApiResultError(code = 400, "User not exist.")
                }
            })
        }

    suspend fun addFriend(userEmail: String, friendEmail: String): ApiResponse<Unit> {
        val userDataSnapshot = getExistUser(userEmail)
        if (userDataSnapshot is ApiResultSuccess) {
            val key =
                userDataSnapshot.data.key ?: return ApiResultError(code = 400, "Doesn't have key.")
            val ref = userRef.child(key).child(PATH_USER_FRIEND_ID_LIST)
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val friendListType = object : GenericTypeIndicator<MutableList<String>>() {}
                    val friendList = snapshot.getValue(friendListType) ?: mutableListOf()
                    friendList.add(friendEmail)
                    ref.setValue(friendList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "databaseError: $error")
                }
            })
            return ApiResultSuccess(Unit)
        } else {
            return ApiResultError(code = 400, "Not exist User.")
        }
    }

    suspend fun removeFriend(userEmail: String, friendEmail: String): ApiResponse<Unit> {
        val userDataSnapshot = getExistUser(userEmail)
        if (userDataSnapshot is ApiResultSuccess) {
            val key =
                userDataSnapshot.data.key ?: return ApiResultError(code = 400, "Doesn't have key.")
            val ref = userRef.child(key).child(PATH_USER_FRIEND_ID_LIST)
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val friendListType = object : GenericTypeIndicator<MutableList<String>>() {}
                    val friendList = snapshot.getValue(friendListType) ?: mutableListOf()
                    friendList.remove(friendEmail)
                    ref.setValue(friendList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "databaseError: $error")
                }
            })
            return ApiResultSuccess(Unit)
        } else {
            return ApiResultError(code = 400, "Not exist User.")
        }
    }

    companion object {
        const val TAG = "UserRemoteDataSource"
    }
}