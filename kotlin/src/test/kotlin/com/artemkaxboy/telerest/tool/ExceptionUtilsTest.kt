package com.artemkaxboy.telerest.tool

import com.artemkaxboy.telerest.tool.Constants.UNKNOWN_ERROR
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class ExceptionUtilsTest {

    @Test
    fun getDeepestMessageOrDefaultTest() {
        val expected1 = UNKNOWN_ERROR
        val exception1 = Exception()
        val message1 = ExceptionUtils.getDeepestMessageOrDefault(exception1)
        Assertions.assertEquals(expected1, message1)

        val expected2 = "No idea what happened"
        val exception2 = Exception()
        val actual2 = ExceptionUtils.getDeepestMessageOrDefault(exception2, expected2)
        Assertions.assertEquals(expected2, actual2)

        val expected3 = "Error3"
        val exception3 = Exception("message1", Exception("message2", Exception(Exception(expected3))))
        val actual3 = ExceptionUtils.getDeepestMessageOrDefault(exception3, expected3)
        Assertions.assertEquals(expected3, actual3)

        val expected4 = "Error4"
        val exception4 = Exception(Exception("message1", Exception(expected4, Exception())))
        val actual4 = ExceptionUtils.getDeepestMessageOrDefault(exception4, expected4)
        Assertions.assertEquals(expected4, actual4)

        // in this case message should contain nested exception type and message if it exists, e.g.
        // "java.lang.Exception"
        // "org.springframework.web.server.ResponseStatusException: 500 INTERNAL_SERVER_ERROR "Try again later""
        val unexpected5 = UNKNOWN_ERROR
        val exception5 = Exception(Exception())
        val message5 = ExceptionUtils.getDeepestMessageOrDefault(exception5)
        Assertions.assertFalse(message5.isEmpty())
        Assertions.assertNotEquals(unexpected5, message5)
    }
}
