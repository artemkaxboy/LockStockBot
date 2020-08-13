package com.artemkaxboy.telerest.tool.extensions

import java.time.Duration

/**
 * Converts [Duration] to human readable string, e.g. "3h 5m", "2m 30s", "0.5s"
 */
fun Duration.toHuman(): String =
    toString().drop(2)
        .replace(Regex("([HM])(\\d)"), "$1 $2")
        .toLowerCase()
