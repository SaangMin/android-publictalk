package com.skysmyoo.publictalk.data.model.remote

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.PropertyName
import com.squareup.moshi.JsonClass
import java.io.Serializable

@Entity(tableName = "saved_user_models")
@JsonClass(generateAdapter = true)
data class User(
    @PrimaryKey(autoGenerate = true)
    @PropertyName("uid") val uid: Int = 0,
    @PropertyName("userEmail") val userEmail: String = "",
    @PropertyName("userName") val userName: String = "",
    @PropertyName("userPhoneNumber") val userPhoneNumber: String = "",
    @PropertyName("userProfileImage") val userProfileImage: String? = null,
    @PropertyName("userLanguage") val userLanguage: String = "",
    @PropertyName("userDeviceToken") var userDeviceToken: String = "",
    @PropertyName("userFriendIdList") var userFriendIdList: List<Friend> = emptyList(),
    @PropertyName("userCreatedAt") val userCreatedAt: String = "",
) : Serializable