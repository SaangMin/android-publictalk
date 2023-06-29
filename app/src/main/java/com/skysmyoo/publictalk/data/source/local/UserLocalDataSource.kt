package com.skysmyoo.publictalk.data.source.local

import com.skysmyoo.publictalk.data.model.local.SavedFriend
import com.skysmyoo.publictalk.data.model.remote.Friend
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.remote.FirebaseData.setUserInfo
import javax.inject.Inject

class UserLocalDataSource @Inject constructor(
    private val userModelDao: UserModelDao,
    private val friendModelDao: FriendModelDao,
    private val preferencesManager: SharedPreferencesManager,
) {

    suspend fun insertUser(user: User) {
        userModelDao.clearUser()
        with(preferencesManager) {
            saveMyEmail(user.userEmail)
            setLocale(user.userLanguage)
        }
        userModelDao.insertUser(user)
        setUserInfo()
    }

    suspend fun getMyInfo(email: String): User? {
        return userModelDao.getMyInfo(email)
    }

    suspend fun clearUser() {
        userModelDao.clearUser()
    }

    suspend fun getUser(): List<User>? {
        return userModelDao.getUserList()
    }

    fun getMyEmail(): String? {
        return preferencesManager.getMyEmail()
    }

    fun getMyLocale(): String {
        return preferencesManager.getLocale()
    }

    fun clearMyData() {
        return preferencesManager.clearUserData()
    }

    suspend fun clearFriendsData() {
        return friendModelDao.clearFriends()
    }

    suspend fun addFriend(myInfo: User, friend: User) {
        val updateFriendList = myInfo.userFriendIdList.toMutableList()
        updateFriendList.add(Friend(userEmail = friend.userEmail))
        friendModelDao.insertFriend(SavedFriend(friendData = friend))

        myInfo.userFriendIdList = updateFriendList
        userModelDao.updateUser(myInfo)
    }

    suspend fun removeFriend(myInfo: User, friend: User) {
        val updateFriendList =
            myInfo.userFriendIdList.toMutableList()
        val removeTarget = updateFriendList.find { it.userEmail == friend.userEmail }
        updateFriendList.remove(removeTarget)
        friendModelDao.removeFriend(friend)

        val updatedMyInfo = myInfo.copy(userFriendIdList = updateFriendList)
        userModelDao.updateUser(updatedMyInfo)
    }

    suspend fun getFriendList(): List<User> {
        val savedFriendList = friendModelDao.getFriendList()
        return savedFriendList.map { it.friendData }
    }
}