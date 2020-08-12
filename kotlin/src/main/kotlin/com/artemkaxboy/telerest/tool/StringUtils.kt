package com.artemkaxboy.telerest.tool

object StringUtils {

    operator fun String.times(i: Int): String {
        if (i <= 0) return ""
        if (i == 1) return this

        val builder = StringBuilder(this)
        repeat((2..i).count()) {
            builder.append(this)
        }
        return builder.toString()
    }

    /**
     * Splits this string to a pair of strings around occurrences of the specified [delimiter].
     */
    operator fun String.div(delimiter: String): Pair<String, String?> {

        return split(delimiter, limit = 2).let { it.first() to it.getOrNull(1) }
    }
}
