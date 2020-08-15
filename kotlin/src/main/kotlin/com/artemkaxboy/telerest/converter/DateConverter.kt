package com.artemkaxboy.telerest.converter

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

fun Date.toLocalDateTime(): LocalDateTime {
    return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
}

fun Date.toLocalDate(): LocalDate {
    return LocalDate.ofInstant(toInstant(), ZoneId.systemDefault())
}
