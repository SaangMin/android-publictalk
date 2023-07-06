package com.skysmyoo.publictalk.di

import android.content.Context
import androidx.room.Room
import com.skysmyoo.publictalk.data.source.local.AppDatabase
import com.skysmyoo.publictalk.data.source.local.ChatModelDao
import com.skysmyoo.publictalk.data.source.local.FriendModelDao
import com.skysmyoo.publictalk.data.source.local.SharedPreferencesManager
import com.skysmyoo.publictalk.data.source.local.UserModelDao
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    val moshi: Moshi by lazy { Moshi.Builder().build() }

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "user-database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(appDatabase: AppDatabase): UserModelDao {
        return appDatabase.userModelDao()
    }

    @Provides
    fun provideFriendDao(appDatabase: AppDatabase): FriendModelDao {
        return appDatabase.friendModelDao()
    }

    @Provides
    fun provideChatDao(appDatabase: AppDatabase): ChatModelDao {
        return appDatabase.chatModelDao()
    }

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): SharedPreferencesManager {
        return SharedPreferencesManager(context)
    }
}