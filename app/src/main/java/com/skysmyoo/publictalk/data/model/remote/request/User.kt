package com.skysmyoo.publictalk.data.model.remote.request

data class User(
    val userEmail: String,
    val userName: String,
    val userPhoneNumber: String,
    val userProfileImage: String,
    val userLanguage: String,
    val userDeviceToken: String,
    val userFriendIdList: List<String>?,
    val userCreatedAt: String,
)