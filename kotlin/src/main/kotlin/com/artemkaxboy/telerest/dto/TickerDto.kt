package com.artemkaxboy.telerest.dto

@Suppress("MemberVisibilityCanBePrivate") // DTO needs data
class TickerDto(

    val id: String = "",

    val url: String = "",

    val currency: String = "",

    val name: String = "",

    val logo: String = ""
) : AbstractDto {

    fun copy(
        id: String? = null,
        url: String? = null,
        currency: String? = null,
        name: String? = null,
        logo: String? = null
    ): TickerDto = TickerDto(
        id ?: this.id,
        url ?: this.url,
        currency ?: this.currency,
        name ?: this.name,
        logo ?: this.logo
    )
}
