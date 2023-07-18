package com.skysmyoo.publictalk.service

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.data.model.remote.Token
import com.skysmyoo.publictalk.data.source.remote.FcmClient
import com.skysmyoo.publictalk.data.source.remote.response.ApiResultSuccess
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmClient: FcmClient

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = fcmClient.registerToken(Token(token))
            if (response is ApiResultSuccess) {
                Log.d(TAG, "Token registered successfully")
            } else {
                Log.e(TAG, "Failed to register token")
            }
        }
    }


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        message.notification?.let {
            showNotification(it.title, it.body)
        }
    }

    private fun showNotification(title: String?, body: String?) {
        val notificationBuilder = NotificationCompat.Builder(this, "channel_id")
            .setSmallIcon(R.drawable.icon_logo_min)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "FMS"
    }
}