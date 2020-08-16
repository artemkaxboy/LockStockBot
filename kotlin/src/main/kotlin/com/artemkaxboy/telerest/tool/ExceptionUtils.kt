package com.artemkaxboy.telerest.tool

import java.net.UnknownHostException

object ExceptionUtils {

    fun String.prefixReasonIfNeeded(prefix: String? = null): String {
        return prefix
            .takeUnless { it.isNullOrBlank() }
            ?.let { this.prefixReason(it) }
            ?: this
    }

    fun String.prefixReason(prefix: String): String = "$prefix: $this"

    fun Throwable.getMessage(prefix: String? = null): String {
        return getDetailedMessage(this).prefixReasonIfNeeded(prefix)
    }

    private fun getDetailedMessage(throwable: Throwable): String {
        return when (throwable) {

            is UnknownHostException ->
                throwable.message?.prefixReason("Unknown host")

            is NullPointerException ->
                "Null pointer exception"

            else ->
                throwable.message
        } ?: throwable.toString()
    }
}
