package com.artemkaxboy.telerest.mapper

import com.artemkaxboy.telerest.dto.LiveDataDto
import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.tool.NumberUtils
import com.artemkaxboy.telerest.tool.extensions.toString
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
            url = source.ticker.url,
            currency = source.ticker.currencyId,
            name = source.ticker.name,
            logo = source.ticker.logo,
            forecast = source.consensus,
            potential = NumberUtils.getDiffOrNull(source.price, source.consensus)
                ?.let { "${it.toString(2)}%" }
                ?: ""
        )
    }

    companion object {

        lateinit var instance: LiveDataToLiveDataDtoMapper private set
    }
}
