package com.skysmyoo.publictalk.data.model.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChattingMember(
    val userEmail: String = "",
    val isChatting: Boolean = false,
)
