package com.skysmyoo.publictalk.data.model.remote

import androidx.room.PrimaryKey
import com.google.firebase.database.PropertyName
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Friend(
    @PrimaryKey(autoGenerate = true)
    @PropertyName("uid") val uid: Long = 0,
    @PropertyName("userEmail") val userEmail: String = "",
): Serializable