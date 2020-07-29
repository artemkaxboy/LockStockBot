package com.artemkaxboy.telerest.dto

import java.time.LocalDate

@Suppress("unused") // DTO needs data
data class LiveDataDto(

    val ticker: String = "",

    val url: String = "",

    val currency: String = "",

    val name: String = "",

    val logo: String = "",

    val date: LocalDate = LocalDate.now(),

    val price: Double = 0.0,

    val forecast: Double? = null,

    val potential: String = ""

) : AbstractDto
