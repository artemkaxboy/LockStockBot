package com.artemkaxboy.telerest.mapper

import com.artemkaxboy.telerest.converter.toLocalDate
import com.artemkaxboy.telerest.converter.toLocalDateTime
import com.artemkaxboy.telerest.dto.Source1ForecastDto
import com.artemkaxboy.telerest.entity.Forecast
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class ForecastToSource1ForecastDtoMapper(mapper: ModelMapper) :
    AbstractMapper<Forecast, Source1ForecastDto>(mapper) {

    @PostConstruct
    fun setupMapper() {
        createTypeMaps()
        instance = this
    }

    override fun postConvert(source: Source1ForecastDto, destination: Forecast): Forecast {
        return destination.copy(
            upstreamId = source.id,
            source = 1,
            publishDate = source.publishDate.toLocalDateTime(),
            expirationDate = source.expirationDate.toLocalDate(),
            targetPrice = source.sharePrice,
            tickerId = ""
        )
    }

    companion object {

        lateinit var instance: ForecastToSource1ForecastDtoMapper private set
    }
}
