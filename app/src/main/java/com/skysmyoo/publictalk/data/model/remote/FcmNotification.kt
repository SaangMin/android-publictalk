package com.skysmyoo.publictalk.data.model.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FcmNotification(
    val to: String,
    val notification: NotificationData,
)

@JsonClass(generateAdapter = true)
data class NotificationData(
    val title: String,
    val body: String,
)