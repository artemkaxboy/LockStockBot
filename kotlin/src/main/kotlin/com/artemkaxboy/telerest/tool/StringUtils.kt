package com.artemkaxboy.telerest.tool

object StringUtils {

    /**
     * Repeats string [i] times. No spaces, no glue.
     *
     * @return repeated string when [i] greater than 1, empty string - otherwise.
     */
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
     *
     * @return pair of strings without [delimiter] itself when [delimiter] is in the string,
     * initial string paired with null - otherwise.
     */
    operator fun String.div(delimiter: String): Pair<String, String?> {

        return split(delimiter, limit = 2).let { it.first() to it.getOrNull(1) }
    }
}
