package com.skysmyoo.publictalk.service

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.skysmyoo.publictalk.PublicTalkApplication.Companion.preferencesManager
import com.skysmyoo.publictalk.data.source.remote.FcmClient
import com.skysmyoo.publictalk.data.source.remote.FirebaseData
import com.skysmyoo.publictalk.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmClient: FcmClient

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FirebaseData.token = token
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (preferencesManager.getNotification()) {
            message.notification?.let {
                Intent().also { intent ->
                    intent.action = Constants.MY_NOTIFICATION
                    intent.putExtra(Constants.KEY_MESSAGE_TITLE, it.title)
                    intent.putExtra(Constants.KEY_MESSAGE_BODY, it.body)
                    sendBroadcast(intent)
                }
            }
        }
        Intent().also { intent ->
            intent.action = Constants.REFRESH_CHAT_ROOM_LIST
            sendBroadcast(intent)
        }
    }

    companion object {
        private const val TAG = "FMS"
    }
}