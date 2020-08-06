package com.artemkaxboy.telerest.mapper

import com.artemkaxboy.telerest.dto.Source1TickerDto
import com.artemkaxboy.telerest.entity.Currency
import com.artemkaxboy.telerest.entity.Ticker
import javax.annotation.PostConstruct
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class TickerToSource1TickerDtoMapper(mapper: ModelMapper) :
    AbstractMapper<Ticker, Source1TickerDto>(mapper) {

    @PostConstruct
    fun setupMapper() {
        createTypeMaps()
        instance = this
    }

    override fun postConvert(source: Source1TickerDto, destination: Ticker): Ticker {
        return Ticker(
            id = source.title,
            url = source.frontUrl,
            currency = Currency(source.currency),
            name = source.company.title,
            logo = source.company.logoLink
        )
    }

    companion object {

        lateinit var instance: TickerToSource1TickerDtoMapper private set
    }
}
