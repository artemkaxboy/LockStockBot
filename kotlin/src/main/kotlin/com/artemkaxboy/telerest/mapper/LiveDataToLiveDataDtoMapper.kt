package com.artemkaxboy.telerest.mapper

import com.artemkaxboy.telerest.dto.LiveDataDto
import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.tool.extensions.toString
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class LiveDataToLiveDataDtoMapper(mapper: ModelMapper) :
    AbstractMapper<LiveData, LiveDataDto>(mapper, LiveData::class.java, LiveDataDto::class.java) {

    @PostConstruct
    fun setupMapper() {

        mapper.createTypeMap(entityClass, dtoClass).apply {
            postConverter = dtoPostConverter()
        }
    }

    override fun postConvert(source: LiveData, destination: LiveDataDto): LiveDataDto {
        return destination.apply {
            this.ticker = source.ticker.ticker
            this.url = source.ticker.url
            this.currency = source.ticker.currency.id
            this.name = source.ticker.name
            this.logo = source.ticker.logo
            this.forecast = source.consensus
            this.potential = ((source.consensus - source.price) / source.price * 100).toString(2) + "%"
        }
    }
}
