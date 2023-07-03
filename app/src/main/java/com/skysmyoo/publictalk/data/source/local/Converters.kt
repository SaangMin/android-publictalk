package com.skysmyoo.publictalk.data.source.local

import androidx.room.TypeConverter
import com.skysmyoo.publictalk.di.AppModule
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types

class Converters {

    private val moshi = AppModule.moshi
    private val adapter: JsonAdapter<List<String>> =
        moshi.adapter(Types.newParameterizedType(List::class.java, String::class.java))

    @TypeConverter
    fun listFromString(value: String): List<String>? {
        return adapter.fromJson(value)
    }

    @TypeConverter
    fun fromList(list: List<String>?): String {
        return adapter.toJson(list)
    }
}