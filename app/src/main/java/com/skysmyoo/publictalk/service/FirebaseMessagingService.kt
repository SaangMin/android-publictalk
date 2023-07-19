package com.skysmyoo.publictalk.service

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.skysmyoo.publictalk.data.model.remote.Token
import com.skysmyoo.publictalk.data.source.remote.FcmClient
import com.skysmyoo.publictalk.data.source.remote.response.ApiResultSuccess
import com.skysmyoo.publictalk.utils.Constants
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
            Intent().also { intent ->
                intent.action = Constants.MY_NOTIFICATION
                intent.putExtra(Constants.KEY_MESSAGE_TITLE, it.title)
                intent.putExtra(Constants.KEY_MESSAGE_BODY, it.body)
                sendBroadcast(intent)
            }
        }
    }

    companion object {
        private const val TAG = "FMS"
    }
}