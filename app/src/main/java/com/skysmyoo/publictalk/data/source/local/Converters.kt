package com.skysmyoo.publictalk.data.source.local

import androidx.room.TypeConverter
import com.skysmyoo.publictalk.data.model.remote.Message
import com.skysmyoo.publictalk.data.model.remote.User
import com.skysmyoo.publictalk.di.AppModule
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types

class Converters {

    private val moshi = AppModule.moshi
    private val userAdapter: JsonAdapter<User> =
        moshi.adapter(User::class.java)
    private val messageMapAdapter: JsonAdapter<Map<String, Message>> =
        moshi.adapter(
            Types.newParameterizedType(
                Map::class.java,
                String::class.java,
                Message::class.java
            )
        )
    private val stringListAdapter: JsonAdapter<List<String>> = moshi.adapter(
        Types.newParameterizedType(
            List::class.java,
            String::class.java
        )
    )

    @TypeConverter
    fun listFromString(value: String):List<String>? {
        return stringListAdapter.fromJson(value)
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return stringListAdapter.toJson(list)
    }

    @TypeConverter
    fun messageMapFromString(value: String): Map<String, Message>? {
        return messageMapAdapter.fromJson(value)
    }

    @TypeConverter
    fun fromMessageMap(map: Map<String, Message>?): String {
        return messageMapAdapter.toJson(map)
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