package com.artemkaxboy.telerest.service.forecast.impl

import com.artemkaxboy.telerest.config.properties.ForecastSource1Properties
import com.artemkaxboy.telerest.converter.toLocalDateTime
import com.artemkaxboy.telerest.dto.Source1ForecastDto
import com.artemkaxboy.telerest.dto.Source1TickerDto
import com.artemkaxboy.telerest.service.forecast.ForecastService
import com.artemkaxboy.telerest.tool.ExceptionUtils
import com.artemkaxboy.telerest.tool.Result
import com.artemkaxboy.telerest.tool.orElse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.io.IOException
import java.time.LocalDateTime
import kotlin.math.absoluteValue

@Service
class ForecastServiceImpl1(private val forecastSource1Properties: ForecastSource1Properties) :
    ForecastService {

    private fun fetchPageFromSource(page: Int): Result<Array<Source1TickerDto>> {

        val url = forecastSource1Properties.baseUrl +
            "?type=share" +
            "&limit=${forecastSource1Properties.pageSize}" +
            "&offset=${forecastSource1Properties.pageSize * page}"

        return Result.of {
            RestTemplate().getForObject(url, Array<Source1TickerDto>::class.java)
                .also { logger.trace { "Got source1 page: $page" } }
        }.orElse {
            Result.failure(
                IOException(ExceptionUtils.messageOrDefault(it, prefix = "Cannot fetch data from url $url: "), it)
            )
        }
    }

    /**
     * Gets all tickers from source 1 or less if [ForecastSource1Properties.maxPages] value were reached.
     * @return the full list of tickers from source 1.
     */
    fun getFlow(): Flow<Source1TickerDto> {

        return flow {
            for (i in 0.until(forecastSource1Properties.maxPages)) {
                val pageResult = fetchPageFromSource(i)

                if (pageResult.isFailure()) {
                    logger.error {
                        ExceptionUtils.messageOrDefault(
                            pageResult.exceptionOrNull(),
                            prefix = "Cannot fetch tickers, page $i: "
                        )
                    }
                    break // break doesn't work from scope functions
                }

                val page = requireNotNull(pageResult.getOrNull())

                emitAll(page.asFlow())

                /* break if last page */
                if (page.size != forecastSource1Properties.pageSize) break
            }
        }
            .buffer(forecastSource1Properties.pageSize * forecastSource1Properties.bufferPages)
            .filter(this::dropIncorrect)
            .filter(this::filterByForecasts)
            .onEach(this::calculateConsensus)
    }

    private suspend fun dropIncorrect(ticker: Source1TickerDto): Boolean {
        if (ticker.currency.isEmpty()) {
            logger.debug { "${ticker.company.title} dropped: no currency" }
            return false
        }
        return true
    }

    private suspend fun filterByForecasts(ticker: Source1TickerDto): Boolean {

        return ticker.forecasts
            .takeIf { hasQuorum(it, ticker.title) }

            /* filter out old forecasts */
            ?.filter(this::isForecastActual)
            ?.takeIf { hasQuorum(it, ticker.title) }

            /* map to prices */
            ?.map { it.sharePrice }
            ?.sorted()

            /* cut extremely low forecast */
            ?.let { cutExtremeLow(it, ticker.price) }
            ?.takeIf { hasQuorum(it, ticker.title) }

            /* cut extremely high forecast */
            ?.reversed()
            ?.let { cutExtremeLow(it, ticker.price) }
            ?.takeIf { hasQuorum(it, ticker.title) }

            ?.also { logger.trace { "Filtering ${ticker.company.title} forecasts finished, count: ${it.size}" } }
            ?.let { true }
            ?: false
    }

    suspend fun calculateConsensus(ticker: Source1TickerDto) {
        ticker.consensus = ticker.forecasts.map { it.sharePrice }.average()
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

    private fun hasQuorum(forecasts: Collection<Any>, ticker: String): Boolean {

        if (isQuorumEnabled() && forecasts.size < forecastSource1Properties.quorum) {
            logger.trace { "$ticker dropped: no forecasts quorum" }
            return false
        }
        return true
    }

    /**
     * Checks if the forecast is still actual by comparing its issue date with allowed
     * forecast [ForecastSource1Properties.ttl].
     *
     * @return true if given forecast is still actual, false - otherwise.
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
