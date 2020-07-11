package com.artemkaxboy.telerest.tool.extensions

import kotlin.math.pow
import kotlin.math.roundToLong

fun Double.round(digits: Int) = 10.0.pow(digits).let { factor ->
    (this * factor).roundToLong().toDouble() / factor
}

fun Double.toString(digits: Int) = "%.${digits}f".format(this)
