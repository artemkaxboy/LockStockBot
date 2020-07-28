package com.artemkaxboy.telerest.tool

import com.artemkaxboy.telerest.tool.Constants.UNKNOWN_ERROR

object ExceptionUtils {

    fun messageOrDefault(
        throwable: Throwable?,
        default: String = UNKNOWN_ERROR,
        prefix: String = "",
        postfix: String = ""
    ) = StringUtils.wrap(throwable?.message ?: default, prefix, postfix)

    fun messageOrEmpty(throwable: Throwable?, prefix: String = "", postfix: String = "") =
        throwable?.message
            ?.let { StringUtils.wrap(it, prefix, postfix) }
            ?: ""
}
