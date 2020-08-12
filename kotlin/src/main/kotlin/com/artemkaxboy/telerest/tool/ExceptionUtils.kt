package com.artemkaxboy.telerest.tool

object ExceptionUtils {

    fun Throwable.getMessage(prefix: String? = null): String {
        val mainMessage = prefix?.let { "$it: " } ?: ""
        return "$mainMessage${message ?: toString()}"
    }
}
