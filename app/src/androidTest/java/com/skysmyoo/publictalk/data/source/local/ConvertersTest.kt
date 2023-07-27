package com.skysmyoo.publictalk.data.source.local

import junit.framework.TestCase.assertEquals
import org.junit.Test

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun listFromString_returnsCorrectList() {
        val listJson = """["a", "b", "c"]"""
        val expectedList = listOf("a", "b", "c")
        val resultList = converters.listFromString(listJson)

        assertEquals(expectedList, resultList)
    }

    @Test
    fun fromList_returnsCorrectJson() {
        val list = listOf("a", "b", "c")
        val expectedJson = """["a","b","c"]"""
        val resultJson = converters.fromList(list)

        assertEquals(expectedJson, resultJson)
    }
}