package com.artemkaxboy.telerest.entity

import java.io.Serializable

data class ForecastId(

    val upstreamId: String = "",

    val source: Int = 1

) : Serializable {

    companion object {

        fun of(entity: Forecast): ForecastId = ForecastId(entity.upstreamId, entity.source)
    }
}
