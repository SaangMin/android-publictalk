package com.skysmyoo.publictalk.data.model.remote

import java.io.Serializable

data class ChatRoom(
    val uid: Int,
    val me: String,
    val other: User,
    val messageList: List<Message>?,
    val chatCreatedAt: String,
): Serializable