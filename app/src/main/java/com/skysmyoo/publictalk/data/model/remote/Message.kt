package com.skysmyoo.publictalk.data.model.remote

import android.os.Parcelable
import com.google.firebase.database.PropertyName
import com.skysmyoo.publictalk.utils.TimeUtil
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class Message(
    @PropertyName("sender") val sender: String = "",
    @PropertyName("receiver") val receiver: String = "",
    @PropertyName("body") val body: String = "",
    @PropertyName("translatedText") val translatedText: String = "",
    @PropertyName("isReading") var reading: Boolean = false,
    @PropertyName("createdAt") val createdAt: String = TimeUtil.getCurrentDateString(),
) : Parcelable