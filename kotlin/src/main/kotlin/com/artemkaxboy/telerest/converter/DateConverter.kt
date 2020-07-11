package com.artemkaxboy.telerest.converter

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

fun Date.toLocalDateTime(): LocalDateTime {
    return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
}
