package com.artemkaxboy.telerest.tool

import com.artemkaxboy.telerest.tool.Constants.UNKNOWN_ERROR

object ExceptionUtils {

    fun messageOrDefault(
        result: Result<Any>,
        prefix: String = "",
        postfix: String = "",
        default: String = UNKNOWN_ERROR
    ) = messageOrDefault(result.exceptionOrNull(), prefix, postfix, default)

    fun messageOrDefault(
        throwable: Throwable?,
        prefix: String = "",
        postfix: String = "",
        default: String = UNKNOWN_ERROR
    ) = StringUtils.wrap(throwable?.message ?: default, prefix, postfix)

    fun messageOrEmpty(throwable: Throwable?, prefix: String = "", postfix: String = "") =
        throwable?.message
            ?.let { StringUtils.wrap(it, prefix, postfix) }
            ?: ""

    fun getDeepestMessageOrDefault(throwable: Throwable?, default: String = UNKNOWN_ERROR): String {

        return getThrowableStack(throwable).asReversed()
            .mapNotNull { it.message }
            .firstOrNull() ?: default
    }

    fun Throwable.getMessage(prefix: String? = null): String {
        val mainMessage = prefix?.let { "$it: " } ?: ""
        return "$mainMessage${message ?: toString()}"
    }

    private fun getThrowableStack(throwable: Throwable?): List<Throwable> {
        val stack = throwable?.let { mutableListOf(throwable) }
            ?: return emptyList()

        var next = stack.last().cause
        while (next != null && next !== stack.last()) {
            stack.add(next)
            next = next.cause
        }

        return stack
    }
}
