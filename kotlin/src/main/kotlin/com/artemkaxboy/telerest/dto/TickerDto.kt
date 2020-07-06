package com.artemkaxboy.telerest.dto

@Suppress("unused") // DTO needs data
class TickerDto(

    val ticker: String = "",

    val url: String = "",

    var currency: String = "",

    val name: String = "",

    val logo: String = ""
) : AbstractDto
