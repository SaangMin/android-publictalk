package com.skysmyoo.publictalk

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.room.Room
import com.skysmyoo.publictalk.data.source.local.AppDatabase
import com.skysmyoo.publictalk.data.source.local.SharedPreferencesManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PublicTalkApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java, "user-database"
        )
            .fallbackToDestructiveMigration()
            .build()
        preferencesManager = SharedPreferencesManager(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "public talk notification channel"
        lateinit var db: AppDatabase
        lateinit var preferencesManager: SharedPreferencesManager
    }
}