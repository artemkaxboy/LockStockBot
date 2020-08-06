package com.artemkaxboy.telerest.entity

import org.springframework.beans.factory.annotation.Value
import java.time.LocalDate

// TODO try to get reid of it and use LAZY loading and foreign key id fields
// syntax in fact is correct, unused functions needed for spring projection
@Suppress("SpringElInspection", "unused")
interface LiveDataShallow {

    fun getDate(): LocalDate

    fun getTickerId(): String

    fun getPrice(): Double

    fun getConsensus(): Double

    @Value("#{((target.consensus - target.price) / target.price * 100)}")
    fun getPotential(): Double
}
