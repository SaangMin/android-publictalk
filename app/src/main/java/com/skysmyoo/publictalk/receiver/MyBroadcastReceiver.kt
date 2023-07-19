package com.skysmyoo.publictalk.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.skysmyoo.publictalk.R
import com.skysmyoo.publictalk.utils.Constants

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Constants.MY_NOTIFICATION) {
            val title = intent.getStringExtra(Constants.KEY_MESSAGE_TITLE)
            val body = intent.getStringExtra(Constants.KEY_MESSAGE_BODY)
            showNotification(context, title, body)

            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                context.getSystemService(VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        500,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                vibrator.vibrate(500)
            }
            Toast.makeText(
                context,
                context.getString(R.string.new_message_receive_msg),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showNotification(context: Context, title: String?, body: String?) {
        val notificationBuilder = NotificationCompat.Builder(context, "channel_id")
            .setSmallIcon(R.drawable.icon_logo_min)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MyBroadcastReceiver"
    }
}