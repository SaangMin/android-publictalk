package com.skysmyoo.publictalk.data.source.remote

import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.ktx.storage

object FirebaseData {

    var user: FirebaseUser? = null
    val storage = Firebase.storage
    var token: String? = null

    fun setUserInfo() {
        user = FirebaseAuth.getInstance().currentUser
    }

    fun setDeviceToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            token = task.result
        })
    }

    fun getIdToken(successResult: (idToken: String) -> Unit) {
        user?.let {
            it.getIdToken(true)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val idToken = task.result.token
                        if (idToken != null) {
                            successResult(idToken)
                        } else {
                            Log.e("FirebaseData", "idToken is null")
                        }
                    } else {
                        Log.e("FirebaseData", "get user error")
                    }
                }
        }
    }
}