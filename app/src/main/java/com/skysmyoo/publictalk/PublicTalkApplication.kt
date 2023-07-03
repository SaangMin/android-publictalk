package com.skysmyoo.publictalk

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.skysmyoo.publictalk.data.source.local.SharedPreferencesManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PublicTalkApplication : Application() {

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        preferencesManager = sharedPreferencesManager
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
        lateinit var preferencesManager: SharedPreferencesManager
    }
}