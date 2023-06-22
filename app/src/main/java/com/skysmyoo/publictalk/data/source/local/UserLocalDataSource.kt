package com.skysmyoo.publictalk.data.source.local

import com.skysmyoo.publictalk.data.model.remote.request.User

class UserLocalDataSource(private val userModelDao: UserModelDao) {

    suspend fun insertUser(user: User) {
        userModelDao.insertUser(user)
    }

    suspend fun getMyInfo(email: String): User? {
        return userModelDao.getMyInfo(email)
    }
}