package com.artemkaxboy.telerest.service.forecast.impl

import com.artemkaxboy.telerest.config.properties.ForecastSource1Properties
import com.artemkaxboy.telerest.dto.Source1TickerDto
import com.artemkaxboy.telerest.mapper.TickerToSource1TickerDtoMapper
import com.artemkaxboy.telerest.mapper.toEntity
import com.artemkaxboy.telerest.service.forecast.ForecastSourceService
import com.artemkaxboy.telerest.service.storage.ForecastService
import com.artemkaxboy.telerest.service.storage.TickerService
import com.artemkaxboy.telerest.tool.ExceptionUtils.getMessage
import com.artemkaxboy.telerest.tool.NumberUtils.getPrecision
import com.artemkaxboy.telerest.tool.RandomUtils
import com.artemkaxboy.telerest.tool.Result
import com.artemkaxboy.telerest.tool.extensions.round
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.jsoup.Jsoup
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class ForecastSourceServiceImpl1(
    private val tickerService: TickerService,
    private val forecastService: ForecastService,
    private val forecastSource1Properties: ForecastSource1Properties
) : ForecastSourceService {

    val sourceId = 1

    fun isEnabled(): Boolean = forecastSource1Properties.isEnabled()

    private suspend fun fetchPageFromSource(page: Int): List<Source1TickerDto> {

        val url = forecastSource1Properties.baseUrl

        return WebClient.builder()
            .baseUrl(url)
            .defaultUriVariables(
                mapOf(
                    "limit" to forecastSource1Properties.pageSize,
                    "offset" to (forecastSource1Properties.pageSize * page)
                )
            )
            .build()
            .get()
            .retrieve()
            .bodyToFlux(Source1TickerDto::class.java)
            .doFirst { logger.debug { "Got source1 page: $page" } }
            .collectList()
            .awaitFirst()
    }

    /**
     * Gets all tickers from source 1 or less if [ForecastSource1Properties.maxPages] value were reached.
     * @return the full list of tickers from source 1.
     */
    suspend fun getTickerFlow(): Flow<Source1TickerDto> {
        logger.info { "Getting tickers from source 1..." }

        var lastPageFull = true
        return (0 until forecastSource1Properties.maxPages)
            .asFlow()

            // this code takes last unnecessary loop, to prevent REST request we need this ugly construction
            .map { if (lastPageFull) fetchPageFromSource(it) else emptyList() }
            .takeWhile { lastPageFull }
            .onEach { lastPageFull = it.size == forecastSource1Properties.pageSize }

            .flatMapMerge { it.asFlow() }
            .buffer(forecastSource1Properties.pageSize * forecastSource1Properties.bufferPages)
            .filter(::dropIncorrect)
    }

    suspend fun fetchDetailedPage(tickerId: String, url: String): Result<AnalysisPrice> {
        logger.info { "Getting detailed page for $tickerId..." }
        val error = "Cannot fetch detailed page ($url)"
        val properties = forecastSource1Properties.analystResolver

        RandomUtils.delay()

        return Result.of(error) {
            val dom = withContext(Dispatchers.IO) {
                Jsoup.connect(url).get()
            }

            dom.select(properties.containerSelector).mapNotNull { element ->

                val analysis = element.select(properties.elementSelector)
                    .firstOrNull { it.toString().contains(properties.analystMark) }
                    ?.select(properties.analystValueSelector)
                    ?.text()

                analysis?.let {
                    AnalysisPrice(
                        analyst = it,

                        price = element.select(properties.analysisValueSelector)
                            .text()
                            .replace(",", ".")
                            .replace(Regex("[^\\d.]"), "")
                            .toDoubleOrNull() ?: Double.NaN
                    )
                }?.takeUnless { it.price.isNaN() }
            }.firstOrNull()
                ?: AnalysisPrice("", Double.NaN)
        }
    }

    suspend fun updateAnalysts() {
        if (!forecastSource1Properties.analystResolver.isEnabled()) return

        delay(forecastSource1Properties.analystResolver.startDelay)

        val savedForecasts = forecastService.findAllBySourceIdAndAnalystNull(sourceId)
            .asFlow()

        savedForecasts
            .mapNotNull { forecast ->
                val analysis = fetchDetailedPage(forecast.tickerId, forecast.url)
                    .onFailure { logger.warn { it.getMessage("Cannot find analysis for forecast $forecast") } }
                    .getOrNull()

                analysis
                    ?.takeIf { forecast.targetPrice.round(getPrecision(it.price)) == it.price }
                    ?.let { forecast.copy(analyst = it.analyst) }
                    .also { it ?: logger.warn { "Analysis price is differ from forecast:\n$analysis\n$forecast" } }
            }
            .collect { forecastService.save(it) }

        return
    }

    private suspend fun dropIncorrect(ticker: Source1TickerDto): Boolean {
        if (ticker.currency.isEmpty()) {
            logger.trace { "${ticker.title} dropped: no currency" }
            return false
        }
        return true
    }

    suspend fun saveMissingForecasts(tickerDto: Source1TickerDto) {
        val ticker = TickerToSource1TickerDtoMapper.instance.toEntity(tickerDto)

        val unsaved = ticker.forecasts
            ?.map { it.upstreamId }
            ?.let { forecastService.findAllBySourceIdAndUpstreamIdIn(sourceId, it) }
            /* get only new analysis which have not been saved */
            ?.map { it.upstreamId }
            ?.toSet()
            ?.let { savedIds ->
                ticker.forecasts.filterNot { it.upstreamId in savedIds }
            }

        tickerService.save(ticker.copy(forecasts = unsaved))
    }

    companion object {

        private val logger = KotlinLogging.logger { }
    }
}

data class AnalysisPrice(val analyst: String, val price: Double)
