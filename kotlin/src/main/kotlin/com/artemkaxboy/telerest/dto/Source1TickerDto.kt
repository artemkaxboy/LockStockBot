package com.artemkaxboy.telerest.dto

import com.artemkaxboy.telerest.tool.deserializers.SecondsDateDeserializer
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.util.Date


data class Source1TickerDto(

    val title: String,

    @JsonProperty("fronturl")
    val frontUrl: String,

    val currency: String,

    val company: Source1CompanyDto,

    @JsonProperty("forecasts_ideas")
    val forecasts: List<Source1ForecastDto>,

    val price: Double

) : AbstractDto {

    var consensus: Double = 0.0
}


data class Source1CompanyDto(

    val title: String,

    @JsonProperty("logo_link")
    val logoLink: String
) : AbstractDto


data class Source1ForecastDto(

    val id: String,

    @JsonProperty("publish_date_t")
    @JsonDeserialize(using = SecondsDateDeserializer::class)
    val publishDate: Date,

    @JsonProperty("expiration_date")
    val expiationDate: Date,

    @JsonProperty("share_price")
    val sharePrice: Double
) : AbstractDto
