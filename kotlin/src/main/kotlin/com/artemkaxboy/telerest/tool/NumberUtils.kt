package com.artemkaxboy.telerest.tool

object NumberUtils {

    /**
     * Calculate difference between given numbers, converts it into percents if needed.
     *
     * @return difference value
     */
    fun getDiffOrNull(from: Double, to: Double?, convertToPercent: Boolean = false): Double? {
        return to?.let { it - from }
            ?.let {
                it.takeUnless { convertToPercent }
                    ?: getPercent(it, from)
            }
    }

    fun getPercent(value: Double, base: Double): Double = value / base * 100
}
