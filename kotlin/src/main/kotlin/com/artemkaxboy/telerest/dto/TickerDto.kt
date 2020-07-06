package com.artemkaxboy.telerest.dto

import java.io.Serializable

class TickerDto(

    val ticker: String = "",

    val url: String = "",

    var currency: String = "",

    val name: String = "",

    val logo: String = ""
) : Serializable, AbstractDto
