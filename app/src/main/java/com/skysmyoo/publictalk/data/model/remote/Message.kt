package com.skysmyoo.publictalk.data.model.remote

data class Message(
    val sender: User,
    val receiver: User,
    val body: String,
    val isReading: Boolean,
    val createdAt: String,
)