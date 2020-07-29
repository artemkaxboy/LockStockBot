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
}
