package com.artemkaxboy.telerest.entity

import java.io.Serializable

data class ForecastId(

    val upstreamId: String = "",

    val source: Int = 1

) : Serializable
