package com.artemkaxboy.telerest.mapper

import com.artemkaxboy.telerest.dto.TickerDto
import com.artemkaxboy.telerest.entity.Ticker
import javax.annotation.PostConstruct
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class TickerToTickerDtoMapper(mapper: ModelMapper) :
    AbstractMapper<Ticker, TickerDto>(mapper) {

    @PostConstruct
    fun setupMapper() {
        createTypeMaps()
        instance = this
    }

    override fun postConvert(source: Ticker, destination: TickerDto): TickerDto {
        return destination.copy(currency = source.currencyId)
    }

    companion object {

        lateinit var instance: TickerToTickerDtoMapper private set
    }
}
