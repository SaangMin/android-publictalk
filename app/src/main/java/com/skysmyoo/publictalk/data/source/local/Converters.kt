package com.skysmyoo.publictalk.data.source.local

import androidx.room.TypeConverter
import com.skysmyoo.publictalk.data.model.remote.Friend
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.di.AppModule
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types

class Converters {

    private val moshi = AppModule.moshi
    private val friendListAdapter: JsonAdapter<List<Friend>> =
        moshi.adapter(Types.newParameterizedType(List::class.java, Friend::class.java))
    private val userAdapter: JsonAdapter<User> =
        moshi.adapter(User::class.java)

    @TypeConverter
    fun friendListFromString(value: String): List<Friend>? {
        return friendListAdapter.fromJson(value)
    }

    @TypeConverter
    fun fromFriendList(list: List<Friend>?): String {
        return friendListAdapter.toJson(list)
    }

    @TypeConverter
    fun userFromString(value: String): User? {
        return userAdapter.fromJson(value)
    }

    @TypeConverter
    fun fromUser(user: User?): String {
        return userAdapter.toJson(user)
    }
}