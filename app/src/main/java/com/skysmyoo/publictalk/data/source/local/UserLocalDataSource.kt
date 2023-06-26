package com.skysmyoo.publictalk.data.source.local

import com.skysmyoo.publictalk.PublicTalkApplication.Companion.preferencesManager
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.remote.FirebaseData.setUserInfo

class UserLocalDataSource(private val userModelDao: UserModelDao) {

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
}