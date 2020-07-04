package com.artemkaxboy.telerest.service.forecast.impl

import com.artemkaxboy.telerest.config.properties.ForecastSource1Properties
import com.artemkaxboy.telerest.converter.toLocalDateTime
import com.artemkaxboy.telerest.dto.Source1ForecastDto
import com.artemkaxboy.telerest.dto.Source1TickerDto
import com.artemkaxboy.telerest.service.forecast.ForecastService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import kotlin.math.absoluteValue

@Service
class ForecastServiceImpl1(private val forecastSource1Properties: ForecastSource1Properties) :
    ForecastService {

    /**
     * Returns forecasts page from source 1.
     *
     * @param page needed page of values, starts from 0.
     * @return array of [Source1TickerDto] with size equals to [ForecastSource1Properties.pageSize]
     * or less if it's the last page.
     */
    private fun getPage(page: Int): Array<Source1TickerDto> {

        val url = forecastSource1Properties.baseUrl +
            "?type=share" +
            "&limit=${forecastSource1Properties.pageSize}" +
            "&offset=${forecastSource1Properties.pageSize * page}"

        return RestTemplate().getForObject(url, Array<Source1TickerDto>::class.java)
            .also { logger.debug { "Got source1 page: $page" } }
            ?: throw IllegalStateException("Cannot fetch tickers page from $url")
    }

    /**
     * Gets all tickers from source 1 or less if [ForecastSource1Properties.maxPages] value were reached.
     * @return the full list of tickers from source 1.
     */
    fun getFlow(): Flow<Source1TickerDto> {

        return flow {
            for (i in 0.until(forecastSource1Properties.maxPages)) {
                val page = getPage(i)
                emitAll(page.asFlow())

                /* break if last page */
                if (page.size != forecastSource1Properties.pageSize) break
            }
        }
            .filter(this::dropIncorrect)
            .filter(this::filterByForecasts)
    }

    fun getBufferedFlow() = getFlow()
        .buffer(forecastSource1Properties.pageSize * forecastSource1Properties.bufferPages)

    private suspend fun dropIncorrect(ticker: Source1TickerDto): Boolean {
        if (ticker.currency.isEmpty()) {
            logger.debug { "${ticker.company.title} dropped: no currency" }
            return false
        }
        return true
    }

    private suspend fun filterByForecasts(ticker: Source1TickerDto): Boolean {

        return ticker.forecasts
            .takeIf(this::hasQuorum)

            /* filter out old forecasts */
            ?.filter(this::isForecastActual)
            ?.takeIf(this::hasQuorum)

            /* map to prices */
            ?.map { it.sharePrice }
            ?.sorted()

            /* cut extremely low forecast */
            ?.let { cutExtremeLow(it, ticker.price) }
            ?.takeIf(this::hasQuorum)

            /* cut extremely high forecast */
            ?.reversed()
            ?.let { cutExtremeLow(it, ticker.price) }
            ?.takeIf(this::hasQuorum)

            ?.also { logger.trace { "Filtering ${ticker.company.title} forecasts finished, count: ${it.size}" } }
            ?.let { true }
            ?: false
    }

    /**
     * Drops the lowest value if it further than [ForecastSource1Properties.extremeThreshold] percent of base
     * from the second low value.
     */
    private fun cutExtremeLow(list: List<Double>, base: Double): List<Double> {

        return list.takeIf { it.size > 2 }
            ?.let { (it[0] - it[1]).absoluteValue / base }
            ?.takeIf { it > forecastSource1Properties.extremeThreshold / 100.0 }
            ?.also { logger.trace { "extreme value dropped ${list[0]}" } }
            ?.let { list.drop(1) }
            ?: list
    }

    private fun isQuorumEnabled() = forecastSource1Properties.quorum > 0

    private fun hasQuorum(forecasts: Collection<Any>): Boolean {

        if (isQuorumEnabled() && forecasts.size < forecastSource1Properties.quorum) {
            logger.debug { "Ticker dropped: no forecasts quorum" }
            return false
        }
        return true
    }

    /**
     * Checks if the forecast is still actual by comparing its issue date with allowed
     * forecast [ForecastSource1Properties.ttl].
     *
     * @return true if given forecast is still actual, false - otherwise
     */
    private fun isForecastActual(forecast: Source1ForecastDto): Boolean {
        if (forecastSource1Properties.ttl.isZero) return true

        return forecast.publishDate
            .toLocalDateTime()
            .isAfter(LocalDateTime.now().minus(forecastSource1Properties.ttl))
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
