package com.skysmyoo.publictalk.data.source

import com.skysmyoo.publictalk.data.model.remote.request.User
import com.skysmyoo.publictalk.data.source.local.UserLocalDataSource

class UserRepository(
    private val localDataSource: UserLocalDataSource,
) {

    suspend fun insertUser(user: User) {
        localDataSource.insertUser(user)
    }


    suspend fun getMyInfo(email: String): User? {
        return localDataSource.getMyInfo(email)
    }
}