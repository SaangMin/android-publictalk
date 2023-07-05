package com.skysmyoo.publictalk.data.model.remote

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.PropertyName
import com.squareup.moshi.JsonClass
import java.io.Serializable

@Entity(tableName = "saved_chat_rooms")
@JsonClass(generateAdapter = true)
data class ChatRoom(
    @PrimaryKey(autoGenerate = true)
    @PropertyName("uid") val uid: Int = 0,
    @PropertyName("member") val member: List<String> = emptyList(),
    @PropertyName("messages") val messages: Map<String, Message> = emptyMap(),
    @PropertyName("chatCreatedAt") val chatCreatedAt: String = "",
) : Serializable