package com.skysmyoo.publictalk.utils

import android.content.Context
import android.content.ContextWrapper
import java.util.Locale

class LanguageContextWrapper(base: Context) : ContextWrapper(base) {
    companion object {
        fun wrap(context: Context, language: String): ContextWrapper {
            var copiedContext = context
            val config = copiedContext.resources.configuration
            val sysLocale = config.locales.get(0)

            if (language.isNotEmpty() && !sysLocale.language.equals(language)) {
                val locale = Locale(language)
                Locale.setDefault(locale)
                config.setLocale(locale)
                copiedContext = copiedContext.createConfigurationContext(config)
            }

            return LanguageContextWrapper(copiedContext)
        }
    }
}