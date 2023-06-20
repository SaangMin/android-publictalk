package com.skysmyoo.publictalk.utils

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import java.util.Locale

class LanguageContextWrapper(base: Context) : ContextWrapper(base) {
    companion object {
        fun wrap(context: Context, language: String): ContextWrapper {
            var context = context
            val config = context.resources.configuration
            val sysLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.locales.get(0)
            } else {
                config.locale
            }

            if (language.isNotEmpty() && !sysLocale.language.equals(language)) {
                val locale = Locale(language)
                Locale.setDefault(locale)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    config.setLocale(locale)
                } else {
                    config.locale = locale
                }
                context = context.createConfigurationContext(config)
            }

            return LanguageContextWrapper(context)
        }
    }
}