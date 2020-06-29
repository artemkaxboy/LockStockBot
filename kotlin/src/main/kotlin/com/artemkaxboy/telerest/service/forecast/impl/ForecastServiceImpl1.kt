package com.artemkaxboy.telerest.service.forecast.impl

import com.artemkaxboy.telerest.config.properties.ForecastSource1Properties
import com.artemkaxboy.telerest.converter.toLocalDateTime
import com.artemkaxboy.telerest.dto.Forecast
import com.artemkaxboy.telerest.dto.Source1TickerDto
import com.artemkaxboy.telerest.service.forecast.ForecastService
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import kotlin.math.absoluteValue

private const val PAGE_SIZE = 15
private const val MAX_PAGE = 15

@Service
class ForecastServiceImpl1(private val forecastSource1Properties: ForecastSource1Properties) :
    ForecastService {

    /**
     * Returns forecasts page from source 1.
     */
    private fun getPage(page: Int): Array<Source1TickerDto> {

        val url = forecastSource1Properties.baseUrl +
            "?type=share" +
            "&limit=${forecastSource1Properties.pageSize}" +
            "&offset=${PAGE_SIZE * page}"

        return RestTemplate().getForObject(url, Array<Source1TickerDto>::class.java)
            ?: throw IllegalStateException("Cannot fetch tickers page from $url")
    }

    fun getList(): List<Source1TickerDto> {

        val results = mutableListOf<Source1TickerDto>()

        for (i in 0..MAX_PAGE) {
            val pageResult = getPage(i)
            results.addAll(pageResult)

            if (pageResult.size < PAGE_SIZE) {
                break
            }
        }

        return results
            .also { logger.info { it.size } }
            .filter { it.currency.isNotEmpty() }
            .also { logger.info { it.size } }
            .filter(this::filterByForecasts)
            .also { logger.info { it.size } }
            // .filter { it.forecasts  }
            .toList()
    }

    fun getBaseUrl(): String {
        return forecastSource1Properties.baseUrl
    }

    private fun filterByForecasts(ticker: Source1TickerDto): Boolean {
        logger.info { "${ticker.company.title} count: ${ticker.forecasts.size}" }

        val remainedItemsCount = ticker.forecasts
            .also { if (!isEnoughForecasts(it)) return false }

            /* filter out old forecasts */
            .filter(this::isActualForecast)
            .also { if (!isEnoughForecasts(it)) return false }
            .map { it.sharePrice }
            .sorted()

            /* cut extremely low forecast */
            .let { cutExtremeLow(it, ticker.price) }
            .also { if (!isEnoughForecasts(it)) return false }

            /* cut extremely high forecast */
            .reversed()
            .let { cutExtremeLow(it, ticker.price) }
            .also { if (!isEnoughForecasts(it)) return false }
            .size

        logger.info { "${ticker.company.title} count: $remainedItemsCount" }
        return true
    }

    /**
     * Drop
     */
    private fun cutExtremeLow(list: List<Double>, base: Double): List<Double> {

        return list.takeIf { it.size > 2 }
            ?.takeIf { ((it[0] - it[1]).absoluteValue / base) > forecastSource1Properties.extremeThreshold / 100.0 }
            ?.also { logger.debug { "${it[0]}  ${it[1]}  $base  ${((it[0] - it[1]).absoluteValue / base)}" } }
            ?.also { logger.debug { "dropping value ${it[0]}" } }
            ?.drop(1)
            ?: list
    }

    fun isEnoughForecasts(forecasts: List<Any>): Boolean {
        if (forecasts.size < forecastSource1Properties.minCount) return false
        return true
    }

    /**
     * Checks if the forecast is still actual by comparing its issue date with allowed
     * forecast [ForecastSource1Properties.ttl].
     *
     * @return true if given forecast is still actual, false - otherwise
     */
    private fun isActualForecast(forecast: Forecast): Boolean {
        if (forecastSource1Properties.ttl.isZero) {
            return true
        }

        return forecast.publishDate.toLocalDateTime().isAfter(
            LocalDateTime.now().minus(forecastSource1Properties.ttl)
        )
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
