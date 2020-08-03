package com.artemkaxboy.telerest.entity

import org.springframework.beans.factory.annotation.Value
import java.time.LocalDate

// syntax in fact is correct, unused functions needed for spring projection
@Suppress("SpringElInspection", "unused")
interface LiveDataShallow {

    fun getDate(): LocalDate

    @Value("#{target.ticker.id}")
    fun getTicker(): String

    fun getPrice(): Double

    fun getConsensus(): Double

    @Value("#{((target.consensus - target.price) / target.price * 100)}")
    fun getPotential(): Double
}
