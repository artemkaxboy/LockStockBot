package com.artemkaxboy.telerest.service.forecast.impl

import com.artemkaxboy.telerest.config.properties.ForecastSource1Properties
import com.artemkaxboy.telerest.converter.toLocalDateTime
import com.artemkaxboy.telerest.dto.Source1ForecastDto
import com.artemkaxboy.telerest.dto.Source1TickerDto
import com.artemkaxboy.telerest.service.forecast.ForecastService
import com.artemkaxboy.telerest.tool.ExceptionUtils
import com.artemkaxboy.telerest.tool.NumberUtils.getPercent
import com.artemkaxboy.telerest.tool.Result
import com.artemkaxboy.telerest.tool.orElse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.io.IOException
import java.time.LocalDateTime
import kotlin.math.absoluteValue

@Service
class ForecastServiceImpl1(private val forecastSource1Properties: ForecastSource1Properties) :
    ForecastService {

    fun isEnabled(): Boolean =
        forecastSource1Properties.baseUrl.isNotBlank() &&
            forecastSource1Properties.maxPages > 0 &&
            forecastSource1Properties.pageSize > 0 &&
            forecastSource1Properties.updateInterval.toMillis() > 0

    private suspend fun fetchPageFromSource(page: Int): Result<Array<Source1TickerDto>> {

        val url = forecastSource1Properties.baseUrl +
            "?type=share" +
            "&limit=${forecastSource1Properties.pageSize}" +
            "&offset=${forecastSource1Properties.pageSize * page}"

        return withContext(Dispatchers.IO) {

            Result.of {

                requireNotNull(RestTemplate().getForObject(url, Array<Source1TickerDto>::class.java))
                    .also { logger.trace { "Got source1 page: $page" } }
            }.orElse { exception ->

                val message = ExceptionUtils.messageOrDefault(exception, "Cannot fetch data from url $url: ")
                Result.failure(IOException(message, exception))
            }
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
                        ExceptionUtils.messageOrDefault(pageResult, "Cannot fetch tickers, page $i: ")
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
            // .onEach { delay(50) }
            .onEach(this::calculateConsensus)
    }

    private suspend fun dropIncorrect(ticker: Source1TickerDto): Boolean {
        if (ticker.currency.isEmpty()) {
            logger.trace { "${ticker.title} dropped: no currency" }
            return false
        }
        return true
    }

    suspend fun calculateConsensus(ticker: Source1TickerDto) {
        ticker.consensus = ticker.forecasts
            .takeIf { hasQuorum(it, ticker.title) }

            /* filter out old forecasts */
            ?.filter(::isForecastWithinTtl)
            ?.takeIf { hasQuorum(it, ticker.title) }

            /* map DTOs to prices */
            ?.map { it.sharePrice }
            ?.sorted()

            /* drop extremely low forecast */
            ?.let { dropLowestIfGapExceededThreshold(it, ticker.price, ticker.title) }
            ?.takeIf { hasQuorum(it, ticker.title) }

            /* drop extremely high forecast */
            ?.reversed()
            ?.let { dropLowestIfGapExceededThreshold(it, ticker.price, ticker.title) }
            ?.takeIf { hasQuorum(it, ticker.title) }

            ?.average()
    }

    private fun dropLowestIfGapExceededThreshold(list: List<Double>, basePrice: Double, ticker: String): List<Double> {

        return list.takeIf { it.size > 2 }
            ?.let { getPercent((it[0] - it[1]).absoluteValue, basePrice) }
            ?.takeIf { it > forecastSource1Properties.extremeThreshold }
            ?.also { logger.trace { "$ticker: extreme value dropped ${list[0]}" } }
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

    private fun isForecastWithinTtl(forecast: Source1ForecastDto): Boolean {
        if (forecastSource1Properties.ttl.isZero) return true

        return forecast.publishDate
            .toLocalDateTime()
            .isAfter(LocalDateTime.now().minus(forecastSource1Properties.ttl))
    }

    companion object {

        private val logger = KotlinLogging.logger { }
    }
}
