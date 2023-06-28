package com.skysmyoo.publictalk.data.source

import android.net.Uri
import com.google.firebase.database.DataSnapshot
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.local.UserLocalDataSource
import com.skysmyoo.publictalk.data.source.remote.UserRemoteDataSource
import retrofit2.Response
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val localDataSource: UserLocalDataSource,
    private val remoteDataSource: UserRemoteDataSource,
) {

    suspend fun insertUser(user: User) {
        localDataSource.insertUser(user)
    }

    suspend fun getMyInfo(email: String): User? {
        return localDataSource.getMyInfo(email)
    }

    suspend fun putUser(auth: String, user: User): Response<Map<String, String>> {
        localDataSource.insertUser(user)
        return remoteDataSource.putUser(auth, user)
    }

    suspend fun uploadImage(image: Uri?): String? {
        return remoteDataSource.uploadImage(image)
    }

    suspend fun getUser(): List<User>? {
        return localDataSource.getUser()
    }

    fun getMyEmail(): String? {
        return localDataSource.getMyEmail()
    }

    fun getMyLocale(): String {
        return localDataSource.getMyLocale()
    }

    fun clearMyData() {
        return localDataSource.clearMyData()
    }

    suspend fun getExistUser(email: String?): DataSnapshot? {
        return remoteDataSource.getExistUser(email)
    }

    suspend fun addFriend(myInfo: User, friendEmail: String) {
        localDataSource.addFriendEmail(myInfo, friendEmail)
    }

    suspend fun removeFriend(myInfo: User, friendEmail: String) {
        localDataSource.removeFriendEmail(myInfo, friendEmail)
    }
}