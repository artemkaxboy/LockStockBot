package com.artemkaxboy.telerest.mapper

import com.artemkaxboy.telerest.dto.Source1TickerDto
import com.artemkaxboy.telerest.entity.LiveData
import java.time.LocalDate
import javax.annotation.PostConstruct
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component

@Component
class LiveDataToSource1TickerDtoMapper(mapper: ModelMapper) :
    AbstractMapper<LiveData, Source1TickerDto>(mapper) {

    @PostConstruct
    fun setupMapper() {
        createTypeMaps()
        instance = this
    }

    override fun postConvert(source: Source1TickerDto, destination: LiveData): LiveData {

        return destination.copy(
            date = LocalDate.now(),
            ticker = TickerToSource1TickerDtoMapper.instance.toEntity(source)
        )
    }

    companion object {

        lateinit var instance: LiveDataToSource1TickerDtoMapper private set
    }
}
