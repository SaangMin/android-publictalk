package com.skysmyoo.publictalk.data.source.local

import com.skysmyoo.publictalk.data.model.local.SavedFriend
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

    suspend fun getMyInfo(): User? {
        val myEmail = preferencesManager.getMyEmail() ?: ""
        return userModelDao.getMyInfo(myEmail)
    }

    fun getMyEmail(): String? {
        return preferencesManager.getMyEmail()
    }

    fun clearMyData() {
        return preferencesManager.clearUserData()
    }

    suspend fun clearFriendsData() {
        return friendModelDao.clearFriends()
    }

    suspend fun addFriend(myInfo: User, friend: User) {
        friendModelDao.insertFriend(SavedFriend(userEmail = friend.userEmail, friendData = friend))
        val updatedFriendList = myInfo.userFriendIdList.toMutableList()
        if(!updatedFriendList.contains(friend.userEmail)){
            updatedFriendList.add(friend.userEmail)
        }

        myInfo.userFriendIdList = updatedFriendList
        userModelDao.updateUser(myInfo)
    }

    suspend fun removeFriend(myInfo: User, friend: User) {
        val updatedFriendList = myInfo.userFriendIdList.toMutableList()
        updatedFriendList.remove(friend.userEmail)
        friendModelDao.removeFriend(friend)

        myInfo.userFriendIdList = updatedFriendList
        userModelDao.updateUser(myInfo)
    }

    suspend fun getFriendList(): List<User> {
        val savedFriendList = friendModelDao.getFriendList()
        return savedFriendList.map { it.friendData }
    }

    suspend fun findFriend(email: String): User? {
        val savedFriend = friendModelDao.findFriend(email) ?: return null
        return savedFriend.friendData
    }
}