package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.config.properties.ForecastSource1Properties
import com.artemkaxboy.telerest.entity.Forecast
import com.artemkaxboy.telerest.tool.Constants
import com.artemkaxboy.telerest.tool.NumberUtils
import com.artemkaxboy.telerest.tool.extensions.round
import mu.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.math.absoluteValue

@Service
class ConsensusService(
    private val forecastSource1Properties: ForecastSource1Properties
) {

    fun calculateConsensus(
        tickerId: String,
        forecasts: List<Forecast>
    ): Double? {
        return forecasts
            .takeIf { hasQuorum(tickerId, it) }

            /* drop extremely low and high forecast */
            ?.let { dropExtremesIfGapExceededThreshold(it) }
            ?.takeIf { hasQuorum(tickerId, it) }

            ?.map { it.targetPrice }
            ?.average()
            ?.round(Constants.ROUND_PRECISION)
    }

    private fun hasQuorum(ticker: String, forecasts: Collection<Any>): Boolean {

        if (forecasts.size < forecastSource1Properties.quorum) {
            logger.trace { "$ticker dropped: no forecasts quorum" }
            return false
        }
        return true
    }

    private fun dropExtremesIfGapExceededThreshold(list: List<Forecast>): List<Forecast> {

        val extremeList = list.takeIf { it.size >= 3 }
            ?.sortedBy { it.targetPrice }
            ?.let {

                val firstExtreme = it.takeIf { isThresholdExceeded(it[0].targetPrice, it[1].targetPrice) }?.first()

                val last = list.size - 1
                val lastExtreme =
                    it.takeIf { isThresholdExceeded(it[last].targetPrice, it[last - 1].targetPrice) }?.last()

                listOfNotNull(firstExtreme, lastExtreme)
            }
            ?: emptyList()

        return list - extremeList
    }

    private fun isThresholdExceeded(extreme: Double, normal: Double) =
        NumberUtils.getPercent((extreme - normal).absoluteValue, normal) > forecastSource1Properties.extremeThreshold

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
