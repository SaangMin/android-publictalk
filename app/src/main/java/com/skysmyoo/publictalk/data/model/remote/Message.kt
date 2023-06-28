package com.skysmyoo.publictalk.data.model.remote

data class Message(
    val sender: String,
    val receiver: String,
    val body: String,
    val isReading: Boolean,
    val createdAt: String,
)