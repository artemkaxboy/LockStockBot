package com.artemkaxboy.telerest.tool

object ExceptionUtils {

    fun String.prefixReasonIfNeeded(prefix: String? = null): String {
        return prefix
            .takeUnless { it.isNullOrBlank() }
            ?.let { this.prefixReason(it) }
            ?: this
    }

    fun String.prefixReason(prefix: String): String = "$prefix: $this"

    fun Throwable.getMessage(prefix: String? = null): String {
        return (message ?: toString()).prefixReasonIfNeeded(prefix)
    }
}
