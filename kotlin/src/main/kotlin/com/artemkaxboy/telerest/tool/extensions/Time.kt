package com.artemkaxboy.telerest.tool.extensions

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date

fun LocalDate.toDate() = atStartOfDay(ZoneId.systemDefault()).toDate()

fun ZonedDateTime.toDate() = Date(toInstant().toEpochMilli())
