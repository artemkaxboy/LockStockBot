package com.artemkaxboy.telerest.tool

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class RandomUtilsTest {

    @Test
    fun dateBeforeTest() {
        repeat((1..10).count()) {
            assertTrue(RandomUtils.dateBefore().isBefore(LocalDate.now()))
        }
    }

    @Test
    fun timeBeforeTest() {
        repeat((1..10).count()) {
            assertTrue(RandomUtils.timeBefore().isBefore(LocalDateTime.now()))
        }
    }

    @Test
    fun dateAfterTest() {
        repeat((1..10).count()) {
            assertTrue(RandomUtils.dateAfter().isAfter(LocalDate.now()))
        }
    }

    @Test
    fun timeAfterTest() {
        repeat((1..10).count()) {
            assertTrue(RandomUtils.timeAfter().isAfter(LocalDateTime.now()))
        }
    }

    @Test
    fun urlTest() {
        val url1 = RandomUtils.url()
        assertTrue(url1.startsWith("https://"))

        val id = "superId"
        val url2 = RandomUtils.url(id)
        assertTrue(url2.startsWith("https://"))
        assertTrue(url2.contains(id))

        val filename = "file.html"
        val url3 = RandomUtils.url(filename = filename)
        assertTrue(url3.startsWith("https://"))
        assertTrue(url3.endsWith(filename))
    }
}
