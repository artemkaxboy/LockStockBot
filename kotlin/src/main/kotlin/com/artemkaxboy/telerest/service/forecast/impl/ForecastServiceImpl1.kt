package com.artemkaxboy.telerest.service.forecast.impl

import com.artemkaxboy.telerest.config.properties.ForecastSource1Properties
import com.artemkaxboy.telerest.dto.Source1TickerDto
import com.artemkaxboy.telerest.service.forecast.ForecastService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.reactive.awaitFirst
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class ForecastServiceImpl1(private val forecastSource1Properties: ForecastSource1Properties) :
    ForecastService {

    fun isEnabled(): Boolean =
        forecastSource1Properties.baseUrl.isNotBlank() &&
            forecastSource1Properties.maxPages > 0 &&
            forecastSource1Properties.pageSize > 0 &&
            forecastSource1Properties.updateInterval.toMillis() > 0

    private suspend fun fetchPageFromSource(page: Int): List<Source1TickerDto> {

        val url = forecastSource1Properties.baseUrl +
            "?type={type}" +
            "&limit={limit}" +
            "&offset={offset}"

        return WebClient.builder()
                .baseUrl(url)
                .defaultUriVariables(
                    mapOf(
                        "type" to "share",
                        "limit" to forecastSource1Properties.pageSize.toString(),
                        "offset" to (forecastSource1Properties.pageSize * page).toString()
                    )
                )
                .build()
                .get()
                .retrieve()
                .bodyToFlux(Source1TickerDto::class.java)
                .doFirst { logger.trace { "Got source1 page: $page" } }
                .collectList()
                .awaitFirst()
    }

    /**
     * Gets all tickers from source 1 or less if [ForecastSource1Properties.maxPages] value were reached.
     * @return the full list of tickers from source 1.
     */
    suspend fun getFlow(): Flow<Source1TickerDto> {

        return (0 until forecastSource1Properties.maxPages)
            .asFlow()
            .map { fetchPageFromSource(it) }
            .takeWhile { it.size == forecastSource1Properties.pageSize }
            .flatMapMerge { it.asFlow() }
            .buffer(forecastSource1Properties.pageSize * forecastSource1Properties.bufferPages)
            .filter(this::dropIncorrect)
    }

    private suspend fun dropIncorrect(ticker: Source1TickerDto): Boolean {
        if (ticker.currency.isEmpty()) {
            logger.trace { "${ticker.title} dropped: no currency" }
            return false
        }
        return true
    }

    companion object {

        private val logger = KotlinLogging.logger { }
    }
}
