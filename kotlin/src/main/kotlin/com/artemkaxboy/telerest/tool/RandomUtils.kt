package com.artemkaxboy.telerest.tool

import com.artemkaxboy.telerest.tool.extensions.round
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kotlin.random.Random

object RandomUtils {

    fun price() = Random.nextDouble(50.0, 150.0).round(Constants.PRICE_ROUND_PRECISION)

    fun dateBefore(from: LocalDate = LocalDate.now(), interval: LongRange = (100..365L)): LocalDate =
        from.minusDays(Random.nextLong(interval.first, interval.last))

    fun timeBefore(from: LocalDateTime = LocalDateTime.now(), interval: LongRange = (100..365L)): LocalDateTime =
        from.minusDays(Random.nextLong(interval.first, interval.last))

    fun dateAfter(from: LocalDate = LocalDate.now(), interval: LongRange = (100..365L)): LocalDate =
        from.plusDays(Random.nextLong(interval.first, interval.last))

    fun timeAfter(from: LocalDateTime = LocalDateTime.now(), interval: LongRange = (100..365L)): LocalDateTime =
        from.plusDays(Random.nextLong(interval.first, interval.last))

    fun forecastSource() = 1

    fun forecastId() = string()

    fun url(id: String = string(), filename: String = "") = "http://site.com/$id/$filename"

    fun company(id: String) = "Company $id"

    fun string() = UUID.randomUUID().toString()
}
