package com.skysmyoo.publictalk.utils

import android.content.Context

object LanguageSharedPreferences {
    private const val LANGUAGE_PREFERENCES = "language_preferences"
    private const val LANGUAGE_KEY = "language_key"

    fun setLocale(context: Context, locale: String) {
        val editor = context.getSharedPreferences(LANGUAGE_PREFERENCES, Context.MODE_PRIVATE).edit()
        editor.putString(LANGUAGE_KEY, locale)
        editor.apply()
    }

    fun getLocale(context: Context): String {
        val sharedPreferences =
            context.getSharedPreferences(LANGUAGE_PREFERENCES, Context.MODE_PRIVATE)
        return sharedPreferences.getString(LANGUAGE_KEY, "default") ?: "default"
    }
}
