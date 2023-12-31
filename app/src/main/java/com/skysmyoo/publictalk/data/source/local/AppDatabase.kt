package com.skysmyoo.publictalk.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.skysmyoo.publictalk.data.model.local.SavedFriend
import com.skysmyoo.publictalk.data.model.remote.ChatRoom
import com.skysmyoo.publictalk.data.model.remote.User

@Database(entities = [User::class, SavedFriend::class, ChatRoom::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userModelDao(): UserModelDao
    abstract fun friendModelDao(): FriendModelDao
    abstract fun chatModelDao(): ChatModelDao
}