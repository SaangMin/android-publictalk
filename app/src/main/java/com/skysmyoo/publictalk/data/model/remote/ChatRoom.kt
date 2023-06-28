package com.skysmyoo.publictalk.data.model.remote

data class ChatRoom(
    val uid: Int,
    val me: String,
    val other: User,
    val messageList: List<Message>?,
    val chatCreatedAt: String,
)