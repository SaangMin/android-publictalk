package com.skysmyoo.publictalk.data.source

import android.net.Uri
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.data.source.local.UserLocalDataSource
import com.skysmyoo.publictalk.data.source.remote.UserRemoteDataSource
import retrofit2.Response

class UserRepository(
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

    suspend fun getUser(): List<User>? {
        return localDataSource.getUser()
    }
}