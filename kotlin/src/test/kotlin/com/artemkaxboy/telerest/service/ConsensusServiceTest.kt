package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.config.properties.ForecastSource1Properties
import com.artemkaxboy.telerest.entity.Forecast
import com.artemkaxboy.telerest.tool.Constants
import com.artemkaxboy.telerest.tool.RandomUtils
import com.artemkaxboy.telerest.tool.extensions.round
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class ConsensusServiceTest {

    private val forecastSource1Properties = ForecastSource1Properties()

    private val ttlDays = forecastSource1Properties.ttl.toDays()

    private val quorum = forecastSource1Properties.quorum

    private val extremeThreshold = forecastSource1Properties.extremeThreshold / 100.0

    private val consensusService = ConsensusService(forecastSource1Properties)

    private val dummyTickerId = "---"

    @Test
    fun `pass if cannot find consensus by empty forecast list`() {

        val actual = consensusService.calculateConsensus(dummyTickerId, emptyList())

        assertNull(actual)
    }

    @Test
    fun `pass if cannot find consensus by short forecast list`() {

        /* generate random not exceeded forecasts */
        val forecast = Forecast.random()
            .copy(publishDate = RandomUtils.timeBefore(interval = (1..ttlDays)))

        /* repeat forecast one time less than quorum is */
        val forecasts = repeat(forecast, quorum - 1)

        val actual = consensusService.calculateConsensus(dummyTickerId, forecasts)

        assertNull(actual)
    }

    @Test
    fun `pass if quorum achieved`() {

        /* generate random not exceeded forecasts */
        val forecast = Forecast.random()
            .copy(publishDate = RandomUtils.timeBefore(interval = (1..ttlDays)))

        /* repeat forecast `quorum` times */
        val forecasts = repeat(forecast)

        val actual =
            consensusService.calculateConsensus(dummyTickerId, forecasts)?.round(Constants.PRICE_ROUND_PRECISION)

        assertEquals(forecast.targetPrice, actual)
    }

    @Test
    fun `fail to drop invalid lowest`() {

        /* generate random not exceeded forecasts */
        val forecast = Forecast.random()
            .copy(publishDate = RandomUtils.timeBefore(interval = (1..ttlDays)))

        /* repeat forecast `quorum` times and add invalid lowest */
        val forecasts = repeat(forecast) +
            forecast.copy(targetPrice = forecast.targetPrice * (1 - extremeThreshold * 1.05))

        val actual =
            consensusService.calculateConsensus(dummyTickerId, forecasts)?.round(Constants.PRICE_ROUND_PRECISION)

        assertEquals(forecast.targetPrice, actual)
    }

    @Test
    fun `fail to leave valid lowest`() {

        /* generate random not exceeded forecasts */
        val forecast = Forecast.random()
            .copy(publishDate = RandomUtils.timeBefore(interval = (1..ttlDays)))

        /* repeat forecast `quorum` times and add valid lowest */
        val forecasts = repeat(forecast) +
            forecast.copy(targetPrice = forecast.targetPrice * (1 - extremeThreshold * .95))

        val expected = forecasts.map { it.targetPrice }.average().round(Constants.PRICE_ROUND_PRECISION)
        val actual =
            consensusService.calculateConsensus(dummyTickerId, forecasts)?.round(Constants.PRICE_ROUND_PRECISION)

        assertEquals(expected, actual)
    }

    @Test
    fun `fail to drop invalid highest`() {

        /* generate random not exceeded forecasts */
        val forecast = Forecast.random()
            .copy(publishDate = RandomUtils.timeBefore(interval = (1..ttlDays)))

        /* repeat forecast `quorum` times and add invalid highest */
        val forecasts = repeat(forecast) +
            forecast.copy(targetPrice = forecast.targetPrice * (1 + extremeThreshold * 1.05))

        val actual =
            consensusService.calculateConsensus(dummyTickerId, forecasts)?.round(Constants.PRICE_ROUND_PRECISION)

        assertEquals(forecast.targetPrice, actual)
    }

    @Test
    fun `fail to leave valid highest`() {

        /* generate random not exceeded forecasts */
        val forecast = Forecast.random()
            .copy(publishDate = RandomUtils.timeBefore(interval = (1..ttlDays)))

        /* repeat forecast `quorum` times and add valid lowest */
        val forecasts = repeat(forecast) +
            forecast.copy(targetPrice = forecast.targetPrice * (1 + extremeThreshold * .95))

        val expected = forecasts.map { it.targetPrice }.average().round(Constants.PRICE_ROUND_PRECISION)
        val actual =
            consensusService.calculateConsensus(dummyTickerId, forecasts)?.round(Constants.PRICE_ROUND_PRECISION)

        assertEquals(expected, actual)
    }

    @Test
    fun `fail to drop invalid extremes`() {

        /* generate random not exceeded forecasts */
        val forecast = Forecast.random()
            .copy(publishDate = RandomUtils.timeBefore(interval = (1..ttlDays)))

        /* repeat forecast `quorum` times and add invalid extremes */
        val forecasts = repeat(forecast) +
            forecast.copy(targetPrice = forecast.targetPrice * (1 - extremeThreshold * 1.1)) +
            forecast.copy(targetPrice = forecast.targetPrice * (1 + extremeThreshold * 1.05))

        val actual =
            consensusService.calculateConsensus(dummyTickerId, forecasts)?.round(Constants.PRICE_ROUND_PRECISION)

        assertEquals(forecast.targetPrice, actual)
    }

    @Test
    fun `fail to leave valid extremes`() {

        /* generate random not exceeded forecasts */
        val forecast = Forecast.random()
            .copy(publishDate = RandomUtils.timeBefore(interval = (1..ttlDays)))

        /* repeat forecast `quorum` times and add valid extremes */
        val forecasts = repeat(forecast) +
            forecast.copy(targetPrice = forecast.targetPrice * (1 - extremeThreshold * .9)) +
            forecast.copy(targetPrice = forecast.targetPrice * (1 + extremeThreshold * .95))

        val expected = forecasts.map { it.targetPrice }.average().round(Constants.PRICE_ROUND_PRECISION)
        val actual =
            consensusService.calculateConsensus(dummyTickerId, forecasts)?.round(Constants.PRICE_ROUND_PRECISION)

        assertEquals(expected, actual)
    }

    private fun repeat(forecast: Forecast, times: Int = quorum): List<Forecast> =
        (1..times).map { forecast }
}
