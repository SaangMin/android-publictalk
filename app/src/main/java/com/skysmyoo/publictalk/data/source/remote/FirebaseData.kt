package com.skysmyoo.publictalk.data.source.remote

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
            if(!task.isSuccessful) {
                return@OnCompleteListener
            }
            token = task.result
        })
    }
}