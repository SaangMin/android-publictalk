package com.skysmyoo.publictalk.data.source

import android.net.Uri
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.local.ChatLocalDataSource
import com.skysmyoo.publictalk.data.source.local.UserLocalDataSource
import com.skysmyoo.publictalk.data.source.remote.ChatRemoteDataSource
import com.skysmyoo.publictalk.data.source.remote.UserRemoteDataSource
import retrofit2.Response
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val chatLocalDataSource: ChatLocalDataSource,
    private val userLocalDataSource: UserLocalDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val chatRemoteDataSource: ChatRemoteDataSource,
) {

    suspend fun insertUser(user: User) {
        userLocalDataSource.insertUser(user)
    }

    suspend fun getMyInfo(): User? {
        return userLocalDataSource.getMyInfo()
    }

    suspend fun putUser(auth: String, user: User): Response<Map<String, String>> {
        userLocalDataSource.insertUser(user)
        return userRemoteDataSource.putUser(auth, user)
    }

    suspend fun uploadImage(image: Uri?): String? {
        return userRemoteDataSource.uploadImage(image)
    }

    fun getMyEmail(): String? {
        return userLocalDataSource.getMyEmail()
    }

    fun getMyLocale(): String {
        return userLocalDataSource.getMyLocale()
    }

    fun clearMyData() {
        return userLocalDataSource.clearMyData()
    }

    suspend fun getExistUser(email: String?): Map<String, User>? {
        val userDataSnapshot = userRemoteDataSource.getExistUser(email)
        val userUid = userDataSnapshot?.key ?: return null
        val user = userDataSnapshot.getValue(User::class.java) ?: return null

        return mapOf(userUid to user)
    }

    private suspend fun addLocalFriend(myInfo: User, friend: User) {
        userLocalDataSource.addFriend(myInfo, friend)
    }

    suspend fun updateUser(auth: String, user: User): Response<User>? {
        userLocalDataSource.insertUser(user)
        return userRemoteDataSource.updateUser(auth, user)
    }

    suspend fun updateFriends(myInfo: User, friendList: List<String>): List<User?> {
        val updatedFriendList = userRemoteDataSource.updateFriendsData(friendList)
        userLocalDataSource.clearFriendsData()
        updatedFriendList.forEach {
            if (it != null) {
                addLocalFriend(myInfo, it)
            }
        }
        return updatedFriendList
    }

    suspend fun getFriends(): List<User> {
        return userLocalDataSource.getFriendList()
    }

    suspend fun getChatRooms(): List<ChatRoom>? {
        return chatLocalDataSource.getChatRoomList()
    }

    suspend fun updateChatRoom(auth: String, myEmail: String): List<ChatRoom> {
        val chatRoomList = chatRemoteDataSource.getChatRooms(auth, myEmail)
        chatLocalDataSource.clearChatRooms()
        chatRoomList.forEach {
            chatLocalDataSource.insertChatRoom(it)
        }
        return chatRoomList
    }

    suspend fun findFriend(email: String): User? {
        return userLocalDataSource.findFriend(email)
    }

    suspend fun searchFriendFromRemote(email: String): User? {
        val userDataSnapshot = userRemoteDataSource.getExistUser(email)
        return userDataSnapshot?.getValue(User::class.java)
    }

    suspend fun addFriend(myInfo: User, friend: User) {
        userRemoteDataSource.addFriend(myInfo.userEmail, friend.userEmail)
        userLocalDataSource.addFriend(myInfo, friend)
    }

    companion object {
        private const val TAG = "UserRepository"
    }
}