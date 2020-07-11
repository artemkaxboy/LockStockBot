package com.artemkaxboy.telerest.mapper

import com.artemkaxboy.telerest.dto.LiveDataDto
import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.tool.extensions.toString
import javax.annotation.PostConstruct
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class LiveDataToLiveDataDtoMapper(mapper: ModelMapper) :
    AbstractMapper<LiveData, LiveDataDto>(mapper, LiveData::class.java, LiveDataDto::class.java) {

    @PostConstruct
    fun setupMapper() {

        mapper.createTypeMap(entityClass, dtoClass).apply {
            postConverter = dtoPostConverter()
        }

        // needed for tests to map json HashMap to object
        mapper.createTypeMap(Map::class.java, dtoClass)
    }

    override fun postConvert(source: LiveData, destination: LiveDataDto): LiveDataDto {
        return destination.copy(
            ticker = source.ticker.ticker,
            url = source.ticker.url,
            currency = source.ticker.currency.id,
            name = source.ticker.name,
            logo = source.ticker.logo,
            forecast = source.consensus,
            potential = ((source.consensus - source.price) / source.price * 100).toString(2) + "%"
        )
    }
}
