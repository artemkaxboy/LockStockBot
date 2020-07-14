package com.artemkaxboy.telerest.entity

import org.springframework.beans.factory.annotation.Value
import java.time.LocalDate

interface LiveDataShallow {

    fun getDate(): LocalDate

    @Value("#{target.ticker.ticker}")
    fun getTicker(): String

    fun getPrice(): Double

    fun getConsensus(): Double
}