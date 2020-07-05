package com.artemkaxboy.telerest.dto

import java.io.Serializable
import java.time.LocalDate

@Suppress("unused") // DTO needs data
class LiveDataDto(

    var ticker: String = "",

    var url: String = "",

    var currency: String = "",

    var name: String = "",

    var logo: String = "",

    var date: LocalDate = LocalDate.now(),

    var price: Double = 0.0,

    var forecast: Double = 0.0,

    var potential: String = ""

) : Serializable, AbstractDto
