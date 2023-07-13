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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

    fun putUser(auth: String, user: User): Flow<Map<String, String>?> {
        return flow {
            when (val response = userRemoteDataSource.putUser(auth, user)) {
                is ApiResultSuccess -> {
                    userLocalDataSource.insertUser(user)
                    userLocalDataSource.clearFriendsData()
                    chatLocalDataSource.clearChatRooms()
                    updateChatRooms(auth, getMyEmail() ?: "")
                    emit(response.data)
                }

                else -> emit(null)
            }
        }
    }

    fun getChatRooms(): Flow<List<ChatRoom>> {
        return flow { emit(chatLocalDataSource.getChatRoomList() ?: emptyList()) }
    }

    fun uploadImage(image: Uri?): Flow<String?> {
        return flow {
            when (val response = userRemoteDataSource.uploadImage(image)) {
                is ApiResultSuccess -> {
                    emit(response.data)
                }

                else -> emit(null)
            }
        }
    }

    fun getMyEmail(): String? {
        return userLocalDataSource.getMyEmail()
    }

    fun getChatRoom(member: List<String>): Flow<ChatRoom?> {
        return flow {
            val chatRoomList = chatLocalDataSource.getChatRoomList() ?: return@flow emit(null)
            for (chatRoom in chatRoomList) {
                val chatRoomMember = chatRoom.member.map { it.userEmail }
                if (member == chatRoomMember) {
                    return@flow emit(chatRoom)
                }
            }
            return@flow emit(null)
        }
    }

    fun clearMyData() {
        return userLocalDataSource.clearMyData()
    }

    fun updateChatRooms(auth: String, email: String): Flow<List<ChatRoom>> {
        return flow {
            when (val response = chatRemoteDataSource.getChatRooms(auth)) {
                is ApiResultSuccess -> {
                    val chatRooms = response.data.filterValues {
                        it.member.map { member -> member.userEmail }.contains(email)
                    }
                    chatLocalDataSource.clearChatRooms()
                    chatRooms.values.forEach {
                        chatLocalDataSource.insertChatRoom(it)
                    }
                    emit(chatRooms.values.toList())
                }

                else -> emit(chatLocalDataSource.getChatRoomList() ?: emptyList())
            }
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

    fun updateUser(auth: String, user: User): Flow<User?> {
        return flow {
            when (val response = userRemoteDataSource.updateUser(auth, user)) {
                is ApiResultSuccess -> {
                    userLocalDataSource.insertUser(user)
                    emit(response.data)
                }

                else -> emit(userLocalDataSource.getMyInfo())
            }
        }
    }

    fun updateFriends(myInfo: User, friendList: List<String>): Flow<List<User?>> {
        return flow {
            when (val response = userRemoteDataSource.updateFriendsData(friendList)) {
                is ApiResultSuccess -> {
                    userLocalDataSource.clearFriendsData()
                    response.data.forEach {
                        if (it != null) {
                            addLocalFriend(myInfo, it)
                        }
                    }
                    emit(response.data)
                }

                else -> emit(userLocalDataSource.getFriendList())
            }
        }
    }

    fun getFriends(): Flow<List<User>> {
        return flow { emit(userLocalDataSource.getFriendList()) }
    }

    fun findFriend(email: String): Flow<User?> {
        return flow { emit(userLocalDataSource.findFriend(email)) }
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