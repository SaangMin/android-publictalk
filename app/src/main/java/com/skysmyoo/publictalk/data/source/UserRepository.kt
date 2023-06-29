package com.skysmyoo.publictalk.data.source

import android.net.Uri
import com.skysmyoo.publictalk.data.model.remote.Friend
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

    fun getMyEmail(): String? {
        return localDataSource.getMyEmail()
    }

    fun getMyLocale(): String {
        return localDataSource.getMyLocale()
    }

    fun clearMyData() {
        return localDataSource.clearMyData()
    }

    suspend fun getExistUser(email: String?): Map<String, User>? {
        val userDataSnapshot = remoteDataSource.getExistUser(email)
        val userUid = userDataSnapshot?.key ?: return null
        val user = userDataSnapshot.getValue(User::class.java) ?: return null

        return mapOf(userUid to user)
    }

    private suspend fun addFriend(myInfo: User, friend: User) {
        localDataSource.addFriend(myInfo, friend)
    }

    suspend fun removeFriend(myInfo: User, friend: User) {
        localDataSource.removeFriend(myInfo, friend)
    }

    suspend fun updateUser(auth: String, user: User): Response<User>? {
        localDataSource.insertUser(user)
        return remoteDataSource.updateUser(auth, user)
    }

    suspend fun updateFriends(myInfo: User, friendList: List<Friend>): List<User?> {
        val friendEmailList = friendList.map { it.userEmail }
        val updatedFriendList = remoteDataSource.updateFriendsData(friendEmailList)
        updatedFriendList.forEach {
            if (it != null) {
                localDataSource.clearFriendsData()
                addFriend(myInfo, it)
            }
        }
        return updatedFriendList
    }

    suspend fun getFriends(): List<User> {
        return localDataSource.getFriendList()
    }
}