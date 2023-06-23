package com.skysmyoo.publictalk.data.source.remote

import com.skysmyoo.publictalk.data.model.remote.request.User
import retrofit2.Response

class LoginRemoteDataSource(private val apiClient: ApiClient) {
    suspend fun putUser(auth: String, user: User): Response<Map<String, String>> {
        return apiClient.putUser(auth, user)
    }
}