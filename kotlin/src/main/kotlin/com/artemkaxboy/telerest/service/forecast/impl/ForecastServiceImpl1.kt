package com.artemkaxboy.telerest.service.forecast.impl

import com.artemkaxboy.telerest.config.properties.ForecastSource1Properties
import com.artemkaxboy.telerest.dto.Source1TickerDto
import com.artemkaxboy.telerest.service.forecast.ForecastService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

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

            if (pageResult.size < PAGE_SIZE) {
                break
            }
        }

        return results.filter { it.currency.isNotEmpty() }.toList()
    }

    fun getBaseUrl(): String {
        return forecastSource1Properties.baseUrl
    }
}
