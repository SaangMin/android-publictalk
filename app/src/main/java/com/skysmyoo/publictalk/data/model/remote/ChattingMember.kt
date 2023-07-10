package com.skysmyoo.publictalk.data.model.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChattingMember(
    val isChatting: Boolean = false,
    val userEmail: String = "",
)
