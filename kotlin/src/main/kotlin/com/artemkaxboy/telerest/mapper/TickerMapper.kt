package com.artemkaxboy.telerest.mapper

import com.artemkaxboy.telerest.dto.TickerDto
import com.artemkaxboy.telerest.entity.Ticker
import com.artemkaxboy.telerest.tool.extensions.toString
import org.modelmapper.ModelMapper
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class TickerMapper(mapper: ModelMapper) :
    AbstractMapper<Ticker, TickerDto>(mapper, Ticker::class.java, TickerDto::class.java) {

    @PostConstruct
    fun setupMapper() {

        // mapper.createTypeMap.addMappings { mapper -> mapper.skip(TickerDto::currency::set) }
        // doesn't work (even after making dest class open)
        // https://youtrack.jetbrains.com/issue/KT-20869

        mapper.createTypeMap(entityClass, dtoClass).apply {

            // change to `isFullTypeMatchingRequired = true`
            // setPropertyCondition { condition ->
            //     condition
            //         .let { it.sourceType.name to it.destinationType.name }
            //         .let { !disabledConversions.contains(it) }
            // }

            postConverter = dtoPostConverter()
        }

        mapper.createTypeMap(dtoClass, entityClass).apply {
            setPostConverter(entityPostConverter())
        }
    }

    override fun postConvert(source: Ticker, destination: TickerDto): TickerDto {
        return destination.apply {
            this.currency = source.currency.id
            source.liveData.firstOrNull()?.also { data ->
                this.price = data.price
                this.forecast = data.consensus
                this.potential = ((data.consensus - data.price) / data.price * 100).toString(2) + "%"
            }
        }
    }

    // change to `isFullTypeMatchingRequired = true`
    // companion object {
    //     val disabledConversions = setOf(Currency::class.java.name to String::class.java.name)
    // }
}
