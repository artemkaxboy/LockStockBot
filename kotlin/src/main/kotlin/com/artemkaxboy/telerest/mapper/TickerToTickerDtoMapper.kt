package com.artemkaxboy.telerest.mapper

import com.artemkaxboy.telerest.dto.TickerDto
import com.artemkaxboy.telerest.entity.Ticker
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class TickerToTickerDtoMapper(mapper: ModelMapper) :
    AbstractMapper<Ticker, TickerDto>(mapper, Ticker::class.java, TickerDto::class.java) {

    @PostConstruct
    fun setupMapper() {

        mapper.createTypeMap(entityClass, dtoClass).apply {
            postConverter = dtoPostConverter()
        }
    }

    override fun postConvert(source: Ticker, destination: TickerDto): TickerDto {
        return destination.apply {
            this.currency = source.currency.id
        }
    }
}
