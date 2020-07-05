package com.artemkaxboy.telerest.dto

import java.io.Serializable

class TickerDto(

    val ticker: String = "",

    val url: String = "",

    var currency: String = "",

    val name: String = "",

    val logo: String = "",

    var price: Double = 0.0,

    var forecast: Double = 0.0,

    var potential: String = ""
) : Serializable, AbstractDto
