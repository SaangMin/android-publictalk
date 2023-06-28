package com.skysmyoo.publictalk.data.source.local

import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.remote.FirebaseData.setUserInfo
import javax.inject.Inject

class UserLocalDataSource @Inject constructor(
    private val userModelDao: UserModelDao,
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

    suspend fun addFriendEmail(myInfo: User, friendEmail: String) {
        val updateFriendList =
            myInfo.userFriendIdList?.toMutableList() ?: emptyList<String>().toMutableList()
        updateFriendList.add(friendEmail)

        val updatedMyInfo = myInfo.copy(userFriendIdList = updateFriendList)
        userModelDao.updateUser(updatedMyInfo)
    }

    suspend fun removeFriendEmail(myInfo: User, friendEmail: String) {
        val updateFriendList =
            myInfo.userFriendIdList?.toMutableList() ?: emptyList<String>().toMutableList()
        updateFriendList.remove(friendEmail)

        val updatedMyInfo = myInfo.copy(userFriendIdList = updateFriendList)
        userModelDao.updateUser(updatedMyInfo)
    }
}