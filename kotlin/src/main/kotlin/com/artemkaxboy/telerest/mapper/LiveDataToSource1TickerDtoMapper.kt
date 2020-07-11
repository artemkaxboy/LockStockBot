package com.artemkaxboy.telerest.mapper

import com.artemkaxboy.telerest.dto.Source1TickerDto
import com.artemkaxboy.telerest.entity.Currency
import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.entity.Ticker
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component
import java.time.LocalDate
import javax.annotation.PostConstruct

@Component
class LiveDataToSource1TickerDtoMapper(mapper: ModelMapper) :
    AbstractMapper<LiveData, Source1TickerDto>(mapper, LiveData::class.java, Source1TickerDto::class.java) {

    @PostConstruct
    fun setupMapper() {

        mapper.createTypeMap(dtoClass, entityClass).apply {
            setPostConverter(entityPostConverter())
        }
    }

    override fun postConvert(source: Source1TickerDto, destination: LiveData): LiveData {
        return destination.copy(
            date = LocalDate.now(),
            ticker = Ticker(
                ticker = source.title,
                url = source.frontUrl,
                currency = Currency(source.currency),
                name = source.company.title,
                logo = source.company.logoLink
            )
        )
    }
}
