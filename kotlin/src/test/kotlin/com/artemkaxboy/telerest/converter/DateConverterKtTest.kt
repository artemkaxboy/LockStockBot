package com.artemkaxboy.telerest.converter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Date

internal class DateConverterKtTest {

    @Test
    fun toLocalDateTimeTest() {
        val date = Date()
        val expect = date.toInstant().epochSecond
        val actual = date.toLocalDateTime().atZone(ZoneId.systemDefault()).toEpochSecond()

        assertEquals(expect, actual)
    }

    @Test
    fun toLocalDateTest() {
        val date = Date()
        val expect = (date.time / 1000).let { it - it % 86400 }
        val actual = date.toLocalDate().toEpochSecond(LocalTime.MIDNIGHT, ZoneOffset.UTC)

        assertEquals(expect, actual)
    }
}
