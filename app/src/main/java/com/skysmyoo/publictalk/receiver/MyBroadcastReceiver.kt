package com.skysmyoo.publictalk.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.utils.Constants

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Constants.MY_NOTIFICATION) {
            Toast.makeText(context, context.getString(R.string.new_message_receive_msg),Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "MyBroadcastReceiver"
    }
}