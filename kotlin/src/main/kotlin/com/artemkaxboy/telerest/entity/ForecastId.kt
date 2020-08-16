package com.artemkaxboy.telerest.entity

import java.io.Serializable

data class ForecastId(

    val sourceId: Int = 1,

    val upstreamId: String = ""

) : Serializable {

    companion object {

        fun of(entity: Forecast): ForecastId = ForecastId(entity.sourceId, entity.upstreamId)
    }
}
