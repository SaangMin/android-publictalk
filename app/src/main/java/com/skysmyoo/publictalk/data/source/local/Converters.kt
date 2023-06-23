package com.skysmyoo.publictalk.data.source.local

import androidx.room.TypeConverter
import com.skysmyoo.publictalk.di.ServiceLocator
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types

class Converters {

    private val moshi = ServiceLocator.moshi
    private val listAdapter: JsonAdapter<List<String>> =
        moshi.adapter(Types.newParameterizedType(List::class.java, String::class.java))

    @TypeConverter
    fun listFromString(value: String): List<String>? {
        return listAdapter.fromJson(value)
    }

    @TypeConverter
    fun fromList(list: List<String>?): String {
        return listAdapter.toJson(list)
    }
}