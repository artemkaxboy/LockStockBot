package com.artemkaxboy.telerest.entity

import java.io.Serializable

data class ForecastId(

    val source: Int = 1,

    val upstreamId: String = ""

) : Serializable {

    companion object {

        fun of(entity: Forecast): ForecastId = ForecastId(entity.source, entity.upstreamId)
    }
}
