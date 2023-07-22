package com.skysmyoo.publictalk

import android.app.Application
import android.os.Build
import com.skysmyoo.publictalk.data.source.local.SharedPreferencesManager
import com.skysmyoo.publictalk.service.NotificationCompatManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PublicTalkApplication : Application() {

    @Inject
    lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompatManager(this).createChannel()
        }
        preferencesManager = sharedPreferencesManager
    }

    companion object {
        lateinit var preferencesManager: SharedPreferencesManager
    }
}