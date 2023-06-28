package com.skysmyoo.publictalk.data.source.local

import android.content.Context
import android.content.SharedPreferences
import com.skysmyoo.publictalk.data.model.local.Constants
import com.skysmyoo.publictalk.data.model.local.Constants.KEY_USER_LANGUAGE
import javax.inject.Inject

class SharedPreferencesManager @Inject constructor(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(Constants.KEY_MY_PREFERENCES, Context.MODE_PRIVATE)

    fun saveMyEmail(email: String) {
        sharedPreferences.edit().putString(Constants.KEY_MY_EMAIL, email).apply()
    }

    fun getMyEmail(): String? {
        return sharedPreferences.getString(Constants.KEY_MY_EMAIL, null)
    }

    fun setLocale(locale: String) {
        sharedPreferences.edit().putString(KEY_USER_LANGUAGE, setLanguage(locale)).apply()
    }

    fun getLocale(): String {
        return sharedPreferences.getString(KEY_USER_LANGUAGE, "ko") ?: "ko"
    }

    fun clearUserData() {
        sharedPreferences.edit().run {
            remove(Constants.KEY_MY_EMAIL)
            apply()
        }
    }

    private fun setLanguage(language: String): String {
        return when (language) {
            "ko" -> "ko"
            else -> "en"
        }
    }
}