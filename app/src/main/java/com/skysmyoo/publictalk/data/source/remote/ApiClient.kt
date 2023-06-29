package com.skysmyoo.publictalk.data.source.remote

import com.skysmyoo.publictalk.data.model.remote.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiClient {

    @POST("users.json")
    suspend fun putUser(
        @Query("auth") auth: String,
        @Body user: User
    ): Response<Map<String, String>>

    @PUT("users/{uid}.json")
    suspend fun updateUser(
        @Path("uid") uid: String,
        @Query("auth") auth: String,
        @Body user: User
    ): Response<Map<String, String>>
}