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

    val company: Company,

    @JsonProperty("forecasts_ideas")
    val forecasts: List<Forecast>,

    val price: Double
)

data class Company(

    val title: String,

    @JsonProperty("logo_link")
    val logoLink: String
)

data class Forecast(

    val id: String,

    @JsonProperty("publish_date_t")
    @JsonDeserialize(using = SecondsDateDeserializer::class)
    val publishDate: Date,

    @JsonProperty("expiration_date")
    val expiationDate: Date,

    @JsonProperty("share_price")
    val sharePrice: Double
)