package com.skysmyoo.publictalk.data.source.remote

import com.skysmyoo.publictalk.data.model.remote.FcmNotification
import com.skysmyoo.publictalk.data.source.remote.response.ApiResponse
import javax.inject.Inject

class FcmDataSource @Inject constructor(private val fcmClient: FcmClient) {

    suspend fun sendNotification(
        serverKey: String,
        notification: FcmNotification
    ): ApiResponse<Unit> {
        return fcmClient.sendNotification(serverKey, notification)
    }
}