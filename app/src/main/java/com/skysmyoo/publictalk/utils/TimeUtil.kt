package com.skysmyoo.publictalk.utils

import android.content.res.Resources
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Calendar
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
        val sdf = SimpleDateFormat(DATE_YEAR_MONTH_DAY_TIME_PATTERN, Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }

    fun convertDateTime(dateTimeString: String): String {
        val date = dateTimeString.toDate(DATE_YEAR_MONTH_DAY_TIME_PATTERN)

        val converterPattern = if (date.isToday()) TIME_PATTERN else DATE_AND_TIME_PATTERN
        val dateFormat = SimpleDateFormat(converterPattern, currentLocale)

        return dateFormat.format(date)
    }

    private fun String.toDate(pattern: String): Date {
        val dateFormat = SimpleDateFormat(pattern, currentLocale)
        return dateFormat.parse(this) ?: Date()
    }

    private fun Date.isToday(): Boolean {
        val calendarTarget = Calendar.getInstance()
        calendarTarget.time = this

        val calendar = Calendar.getInstance()

        return (calendarTarget.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)
                && calendarTarget.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                && calendarTarget.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH))
    }
}