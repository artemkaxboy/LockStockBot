package com.artemkaxboy.telerest.mapper

import com.artemkaxboy.telerest.dto.LiveDataDto
import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.tool.Constants
import com.artemkaxboy.telerest.tool.extensions.round
import org.modelmapper.ModelMapper
import org.modelmapper.spi.MappingContext
import org.springframework.stereotype.Component
import java.time.LocalDate
import javax.annotation.PostConstruct

@Component
class LiveDataToLiveDataDtoMapper(mapper: ModelMapper) :
    AbstractMapper<LiveData, LiveDataDto>(mapper) {

    @PostConstruct
    fun setupMapper() {
        createTypeMaps()
        instance = this

        mapper.addConverter { context: MappingContext<String, LocalDate> ->
            LocalDate.parse(context.source)
        }
    }

    override fun postConvert(source: LiveData, destination: LiveDataDto): LiveDataDto {

        requireNotNull(source.ticker)

        return destination.copy(
            ticker = source.ticker.id,
            currency = source.ticker.currencyId,
            name = source.ticker.name,
            forecast = source.consensus?.round(Constants.PRICE_ROUND_PRECISION),
            potential = source.getRoundedPotential()
        )
    }

    companion object {

        lateinit var instance: LiveDataToLiveDataDtoMapper private set
    }
}
