package com.skysmyoo.publictalk.data.model.remote

import com.google.firebase.database.PropertyName
import com.skysmyoo.publictalk.utils.TimeUtil
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Message(
    @PropertyName("sender") val sender: String = "",
    @PropertyName("receiver") val receiver: String = "",
    @PropertyName("body") val body: String = "",
    @PropertyName("isReading") var reading: Boolean = false,
    @PropertyName("createdAt") val createdAt: String = TimeUtil.getCurrentDateString(),
) : Serializable