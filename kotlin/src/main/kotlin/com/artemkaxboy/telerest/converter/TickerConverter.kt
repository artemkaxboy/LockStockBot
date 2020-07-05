package com.artemkaxboy.telerest.converter

import com.artemkaxboy.telerest.dto.Source1TickerDto
import com.artemkaxboy.telerest.entity.Currency
import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.entity.Ticker
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class Source1TickerDtoToTickerConverter : Converter<Source1TickerDto, Ticker> {

    override fun convert(source: Source1TickerDto): Ticker {
        return Ticker(
            ticker = source.title,
            name = source.company.title,
            url = source.frontUrl,
            currency = Currency(source.currency),
            logo = source.company.logoLink
        ).apply { liveData = listOf(
            LiveData(ticker = this, price = source.price, consensus = source.consensus)
        ) }
    }
}
