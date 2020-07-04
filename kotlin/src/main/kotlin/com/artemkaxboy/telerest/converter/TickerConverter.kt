package com.artemkaxboy.telerest.converter

import com.artemkaxboy.telerest.dto.Source1ForecastDto
import com.artemkaxboy.telerest.dto.Source1TickerDto
import com.artemkaxboy.telerest.model.Currency
import com.artemkaxboy.telerest.model.Forecast
import com.artemkaxboy.telerest.model.Ticker
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class Source1TickerDtoToTickerConverter : Converter<Source1TickerDto, Ticker> {

    override fun convert(source: Source1TickerDto): Ticker {
        val forecastConverter = Source1ForecastDtoToForecastConverter()
        return Ticker(
            ticker = source.title,
            name = source.company.title,
            url = source.frontUrl,
            currency = Currency(source.currency),
            logo = source.company.logoLink,
            price = source.price,
            forecasts = source.forecasts
                .map { forecastConverter.convert(it) }
        )
    }
}

class Source1ForecastDtoToForecastConverter : Converter<Source1ForecastDto, Forecast> {

    override fun convert(source: Source1ForecastDto): Forecast {
        return Forecast(
            publishDate = source.publishDate,
            expiationDate = source.expiationDate,
            targetPrice = source.sharePrice
        )
    }
}
