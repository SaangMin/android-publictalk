package com.skysmyoo.publictalk.data.model.remote

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.io.Serializable

@Entity(
    tableName = "saved_user_models"
)
@JsonClass(generateAdapter = true)
data class User(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    val userEmail: String,
    val userName: String,
    val userPhoneNumber: String,
    val userProfileImage: String?,
    val userLanguage: String,
    val userDeviceToken: String,
    val userFriendIdList: List<String>?,
    val userCreatedAt: String,
) : Serializable