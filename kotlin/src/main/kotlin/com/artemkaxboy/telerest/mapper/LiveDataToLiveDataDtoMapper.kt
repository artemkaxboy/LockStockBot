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
    AbstractMapper<LiveData, LiveDataDto>(mapper, LiveData::class.java, LiveDataDto::class.java) {

    @PostConstruct
    fun setupMapper() {

        mapper.createTypeMap(entityClass, dtoClass).apply {
            postConverter = dtoPostConverter()
        }

        mapper.addConverter { context: MappingContext<String, LocalDate> ->
            LocalDate.parse(context.source)
        }
    }

    override fun postConvert(source: LiveData, destination: LiveDataDto): LiveDataDto {
        return destination.copy(
            ticker = source.ticker.ticker,
            url = source.ticker.url,
            currency = source.ticker.currency.id,
            name = source.ticker.name,
            logo = source.ticker.logo,
            forecast = source.consensus,
            potential = NumberUtils.getDiffOrNull(source.price, source.consensus)
                ?.let { "${it.toString(2)}%" }
                ?: ""
        )
    }
}
