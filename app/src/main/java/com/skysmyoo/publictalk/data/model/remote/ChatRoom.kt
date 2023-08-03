package com.skysmyoo.publictalk.data.model.remote

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.PropertyName
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Entity(tableName = "saved_chat_rooms")
@JsonClass(generateAdapter = true)
@Parcelize
data class ChatRoom(
    @PrimaryKey(autoGenerate = true)
    @PropertyName("uid") val uid: Int = 0,
    @PropertyName("member") val member: List<ChattingMember> = emptyList(),
    @PropertyName("messages") val messages: Map<String, Message> = emptyMap(),
    @PropertyName("chatCreatedAt") val chatCreatedAt: String = "",
) : Parcelable