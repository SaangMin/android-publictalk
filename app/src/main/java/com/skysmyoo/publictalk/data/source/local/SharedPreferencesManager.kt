package com.skysmyoo.publictalk.data.source.local

import android.content.Context
import android.content.SharedPreferences
import com.skysmyoo.publictalk.data.model.local.Constants

class SharedPreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(Constants.KEY_MY_PREFERENCES, Context.MODE_PRIVATE)

    fun saveMyEmail(email: String) {
        sharedPreferences.edit().putString(Constants.KEY_MY_EMAIL, email).apply()
    }

    fun getMyEmail(): String? {
        return sharedPreferences.getString(Constants.KEY_MY_EMAIL, null)
    }

    fun clearUserData() {
        sharedPreferences.edit().run {
            remove(Constants.KEY_MY_EMAIL)
            apply()
        }
    }
}