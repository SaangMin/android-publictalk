package com.skysmyoo.publictalk.data.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.PropertyName
import com.skysmyoo.publictalk.data.model.remote.User
import com.squareup.moshi.JsonClass
import java.io.Serializable

@Entity(tableName = "saved_friend_models")
@JsonClass(generateAdapter = true)
data class SavedFriend(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @PropertyName("friendData") val friendData: User,
) : Serializable
