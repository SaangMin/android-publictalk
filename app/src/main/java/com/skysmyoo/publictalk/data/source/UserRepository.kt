package com.skysmyoo.publictalk.data.source

import android.net.Uri
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.local.ChatLocalDataSource
import com.skysmyoo.publictalk.data.source.local.UserLocalDataSource
import com.skysmyoo.publictalk.data.source.remote.ChatRemoteDataSource
import com.skysmyoo.publictalk.data.source.remote.UserRemoteDataSource
import com.skysmyoo.publictalk.data.source.remote.response.ApiResponse
import com.skysmyoo.publictalk.data.source.remote.response.ApiResultError
import com.skysmyoo.publictalk.data.source.remote.response.ApiResultSuccess
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val chatLocalDataSource: ChatLocalDataSource,
    private val userLocalDataSource: UserLocalDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val chatRemoteDataSource: ChatRemoteDataSource,
) {

    suspend fun getMyInfo(): User? {
        return userLocalDataSource.getMyInfo()
    }

    suspend fun putUser(auth: String, user: User): Map<String, String>? {
        return when (val response = userRemoteDataSource.putUser(auth, user)) {
            is ApiResultSuccess -> {
                userLocalDataSource.insertUser(user)
                response.data
            }

            else -> null
        }
    }

    suspend fun getChatRooms(): List<ChatRoom> {
        return chatLocalDataSource.getChatRoomList() ?: emptyList()
    }

    suspend fun uploadImage(image: Uri?): String? {
        return when (val response = userRemoteDataSource.uploadImage(image)) {
            is ApiResultSuccess -> {
                response.data
            }

            else -> null
        }
    }

    fun getMyEmail(): String? {
        return userLocalDataSource.getMyEmail()
    }

    suspend fun getChatRoom(member: List<String>): ChatRoom? {
        val chatRoomList = chatLocalDataSource.getChatRoomList() ?: return null
        for (chatRoom in chatRoomList) {
            val chatRoomMember = chatRoom.member.map { it.userEmail }
            if (member == chatRoomMember) {
                return chatRoom
            }
        }
        return null
    }

    fun clearMyData() {
        return userLocalDataSource.clearMyData()
    }

    suspend fun updateChatRooms(auth: String, email: String): List<ChatRoom> {
        return when (val response = chatRemoteDataSource.getChatRooms(auth)) {
            is ApiResultSuccess -> {
                val chatRooms = response.data.filterValues {
                    it.member.map { member -> member.userEmail }.contains(email)
                }
                chatRooms.values.toList()
            }

            else -> chatLocalDataSource.getChatRoomList() ?: emptyList<ChatRoom>()
        }
    }

    suspend fun getExistUser(email: String?): ApiResponse<Map<String, User>>? {
        return when (val response = userRemoteDataSource.getExistUser(email)) {
            is ApiResultSuccess -> {
                val userUid = response.data.key ?: return null
                val user = response.data.getValue(User::class.java) ?: return null
                ApiResultSuccess(mapOf(userUid to user))
            }
            else -> null
        }
    }

    private suspend fun addLocalFriend(myInfo: User, friend: User) {
        userLocalDataSource.addFriend(myInfo, friend)
    }

    suspend fun updateUser(auth: String, user: User): User? {
        return when (val response = userRemoteDataSource.updateUser(auth, user)) {
            is ApiResultSuccess -> {
                userLocalDataSource.insertUser(user)
                response.data
            }

            else -> userLocalDataSource.getMyInfo()
        }
    }

    suspend fun updateFriends(myInfo: User, friendList: List<String>): List<User?> {
        return when (val response = userRemoteDataSource.updateFriendsData(friendList)) {
            is ApiResultSuccess -> {
                userLocalDataSource.clearFriendsData()
                response.data.forEach {
                    if (it != null) {
                        addLocalFriend(myInfo, it)
                    }
                }
                response.data
            }

            else -> userLocalDataSource.getFriendList()
        }
    }

    suspend fun getFriends(): List<User> {
        return userLocalDataSource.getFriendList()
    }

    suspend fun findFriend(email: String): User? {
        return userLocalDataSource.findFriend(email)
    }

    suspend fun searchFriendFromRemote(email: String): ApiResponse<User>? {
        return when (val response = userRemoteDataSource.getExistUser(email)) {
            is ApiResultSuccess -> {
                val user = response.data.getValue(User::class.java) ?: return null
                ApiResultSuccess(user)
            }

            is ApiResultError -> {
                ApiResultError(code = 400, "Network Error!")
            }

            else -> null
        }
    }

    suspend fun addFriend(myInfo: User, friend: User): ApiResponse<Unit> {
        return when (val response =
            userRemoteDataSource.addFriend(myInfo.userEmail, friend.userEmail)) {
            is ApiResultSuccess -> {
                userLocalDataSource.addFriend(myInfo, friend)
                response
            }

            else -> response
        }
    }

    suspend fun removeFriend(myInfo: User, friend: User): ApiResponse<Unit> {
        return when (val response =
            userRemoteDataSource.removeFriend(myInfo.userEmail, friend.userEmail)) {
            is ApiResultSuccess -> {
                userLocalDataSource.removeFriend(myInfo, friend)
                response
            }

            else -> response
        }
    }

    companion object {
        private const val TAG = "UserRepository"
    }
}