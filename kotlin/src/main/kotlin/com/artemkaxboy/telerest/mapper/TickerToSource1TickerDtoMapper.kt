package com.artemkaxboy.telerest.mapper

import com.artemkaxboy.telerest.dto.Source1TickerDto
import com.artemkaxboy.telerest.entity.Currency
import com.artemkaxboy.telerest.entity.Ticker
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class TickerToSource1TickerDtoMapper(mapper: ModelMapper) :
    AbstractMapper<Ticker, Source1TickerDto>(mapper) {

    @PostConstruct
    fun setupMapper() {
        createTypeMaps()
        instance = this
    }

    override fun postConvert(source: Ticker, destination: Source1TickerDto): Source1TickerDto {
        val reason = "We never send Source1TickerDto anywhere"
        throw NotImplementedError("An operation is not implemented: $reason")
    }

    override fun postConvert(source: Source1TickerDto, destination: Ticker): Ticker {

        val currency = Currency(source.currency)
        val tickerId = source.title

        return destination.copy(
            id = tickerId,
            url = source.frontUrl,
            currency = currency,
            currencyId = currency.id,
            name = source.company.title,
            logo = source.company.logoLink,
            forecasts = destination.forecasts
                ?.map { it.copy(tickerId = tickerId) }
        )
    }

    companion object {

        lateinit var instance: TickerToSource1TickerDtoMapper private set
    }
}
