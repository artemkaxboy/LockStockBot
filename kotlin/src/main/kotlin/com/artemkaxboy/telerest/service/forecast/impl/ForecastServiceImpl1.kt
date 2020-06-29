package com.artemkaxboy.telerest.service.forecast.impl

import com.artemkaxboy.telerest.config.properties.ForecastSource1Properties
import com.artemkaxboy.telerest.converter.toLocalDateTime
import com.artemkaxboy.telerest.dto.Source1TickerDto
import com.artemkaxboy.telerest.service.forecast.ForecastService
import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.KFunction1

private const val PAGE_SIZE = 15
private const val MAX_PAGE = 15

@Service
class ForecastServiceImpl1(private val forecastSource1Properties: ForecastSource1Properties) :
    ForecastService {

    private fun getPage(page: Int): Array<Source1TickerDto> {

        val url = forecastSource1Properties.baseUrl +
            "?type=share" +
            "&limit=$PAGE_SIZE" +
            "&offset=${PAGE_SIZE * page}"

        return RestTemplate().getForObject(url, Array<Source1TickerDto>::class.java)
            ?: throw IllegalStateException("Cannot fetch tickers page from $url")
    }

    fun getList(): List<Source1TickerDto> {

        val results = mutableListOf<Source1TickerDto>()

        for (i in 0..MAX_PAGE) {
            val pageResult = getPage(i)
            results.addAll(pageResult)

            // if (pageResult.size < PAGE_SIZE) {
            break
            // }
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
        logger.info { "${ticker.company.title} count ${ticker.forecasts.size}" }
        if (ticker.forecasts.size < forecastSource1Properties.minCount) {
            return false
        }

        val actualForecasts = ticker.forecasts
            .filter {
                it.publishDate.toLocalDateTime()
            }
            .toList()

        ticker.forecasts.size
        return true
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
