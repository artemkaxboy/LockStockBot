@file:Suppress("unused") // library functions can be unused

package com.artemkaxboy.telerest.tool.extensions

import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Rounds this [Double] value with needed precision.
 *
 * @param digits needed precision or negative number to skip rounding.
 * @return rounded value if [digits] 0 or positive, original value otherwise.
 */
fun Double.roundIfNeeded(digits: Int) =
    this.takeIf { digits < 0 }
        ?: round(digits)

/**
 * Rounds this [Double] value with needed precision.
 *
 * @param digits needed precision, must be 0 or positive.
 * @return rounded value.
 */
fun Double.round(digits: Int): Double {
    require(digits >= 0)
    return 10.0.pow(digits).let { factor ->
        (this * factor).roundToLong().toDouble() / factor
    }
}

fun Double.toString(digits: Int) = "%.${digits}f".format(this)
