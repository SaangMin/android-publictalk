package com.skysmyoo.publictalk.utils

import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TimeUtilTest {

    @Test
    fun getCurrentDateString_returnsExpectedFormat() {
        val expectedFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'"
        val dateFormat = SimpleDateFormat(expectedFormat, Locale.ROOT)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val expectedDateString = dateFormat.format(Date())

        val resultDateString = TimeUtil.getCurrentDateString()

        assertEquals(expectedDateString, resultDateString)
    }
}