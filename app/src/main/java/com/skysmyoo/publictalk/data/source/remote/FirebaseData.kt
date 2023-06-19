package com.skysmyoo.publictalk.data.source.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object FirebaseData {

    var user: FirebaseUser? = null

    fun setUserInfo() {
        user = FirebaseAuth.getInstance().currentUser
    }
}