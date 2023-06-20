package com.skysmyoo.publictalk.data.model.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotificationModel(
    val to: String,
    val notification: NotificationContent,
)

@JsonClass(generateAdapter = true)
data class NotificationContent(
    val title: String,
    val body: String,
)