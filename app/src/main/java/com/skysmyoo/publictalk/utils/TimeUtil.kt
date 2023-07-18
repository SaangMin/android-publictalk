package com.skysmyoo.publictalk.utils

import android.content.res.Resources
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TimeUtil {
    private const val DATE_YEAR_MONTH_DAY_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    private const val TIME_PATTERN = "a h : mm"
    private const val DATE_AND_TIME_PATTERN = " M / d\n $TIME_PATTERN"

    private val currentLocale: Locale
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Resources.getSystem().configuration.locales.get(0)
            } else {
                Resources.getSystem().configuration.locale
            }
        }

    fun getCurrentDateString(): String {
        val date = Date()
        val utcZone = TimeZone.getTimeZone("UTC")
        val simpleDateFormat = SimpleDateFormat(DATE_YEAR_MONTH_DAY_TIME_PATTERN, Locale.ROOT)
        simpleDateFormat.timeZone = utcZone
        return simpleDateFormat.format(date)
    }

    fun convertDateTime(dateTimeString: String): String {
        if(dateTimeString.isEmpty()) return ""
        val date = dateTimeString.toDate(DATE_YEAR_MONTH_DAY_TIME_PATTERN)
        val converterPattern = if (date.isToday()) TIME_PATTERN else DATE_AND_TIME_PATTERN
        val dateFormat = SimpleDateFormat(converterPattern, currentLocale)
        return dateFormat.format(date)
    }

    private fun String.toDate(pattern: String): Date {
        val dateFormat = SimpleDateFormat(pattern, Locale.ROOT)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.parse(this) ?: Date()
    }

    private fun Date.isToday(): Boolean {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.ROOT)
        val targetDateString = dateFormat.format(this)
        val currentDateString = dateFormat.format(Date())

        return targetDateString.toInt() >= currentDateString.toInt()
    }
}