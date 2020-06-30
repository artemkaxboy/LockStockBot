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
            ?: throw IllegalStateException("Cannot fetch tickers page from $url")
    }

    /**
     * Gets all tickers from source 1 or less if [ForecastSource1Properties.maxPages] value were reached.
     * @return the full list of tickers from source 1.
     */
    fun getList(): List<Source1TickerDto> {

        val results = mutableListOf<Source1TickerDto>()

        for (i in 0..forecastSource1Properties.maxPages) {
            val pageResult = getPage(i)
            results.addAll(pageResult)

            if (pageResult.size < forecastSource1Properties.pageSize) {
                break
            }
        }

        return results
            .also { logger.debug { "Got ${it.size} tickers" } }
            .filter(this::dropIncorrect)
            .filter(this::filterByForecasts)
            .also { logger.debug { "Have ${it.size} tickers after filters" } }
            .toList()
    }

    private fun dropIncorrect(ticker: Source1TickerDto): Boolean {
        if (ticker.currency.isEmpty()) return false
        return true
    }

    private fun filterByForecasts(ticker: Source1TickerDto): Boolean {

        return ticker.forecasts
            .also { logger.debug { "${ticker.company.title} forecasts: ${it.size}" } }
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

            ?.also { logger.debug { "${ticker.company.title} filtered forecasts: ${it.size}" } }
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
            ?.also { logger.debug { "extreme value dropped ${list[0]}" } }
            ?.let { list.drop(1) }
            ?: list
    }

    private fun isQuorumEnabled() = forecastSource1Properties.quorum > 0

    private fun hasQuorum(forecasts: List<Any>): Boolean {

        if (isQuorumEnabled() && forecasts.size < forecastSource1Properties.quorum) {
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
    private fun isForecastActual(forecast: Forecast): Boolean {
        if (forecastSource1Properties.ttl.isZero) return true

        return forecast.publishDate
            .toLocalDateTime()
            .isAfter(LocalDateTime.now().minus(forecastSource1Properties.ttl))
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
