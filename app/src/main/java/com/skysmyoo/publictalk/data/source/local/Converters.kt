package com.skysmyoo.publictalk.data.source.local

import androidx.room.TypeConverter
import com.skysmyoo.publictalk.data.model.remote.Friend
import com.skysmyoo.publictalk.di.AppModule
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types

class Converters {

    private val moshi = AppModule.moshi
    private val adapter: JsonAdapter<List<Friend>> =
        moshi.adapter(Types.newParameterizedType(List::class.java, Friend::class.java))

    @TypeConverter
    fun friendListFromString(value: String): List<Friend>? {
        return adapter.fromJson(value)
    }

    @TypeConverter
    fun fromFriendList(list: List<Friend>?): String {
        return adapter.toJson(list)
    }
}