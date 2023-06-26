package com.skysmyoo.publictalk.data.model.remote

data class Message(
    val me: User,
    val other: User,
    val body: String,
    val createdAt: String,
)