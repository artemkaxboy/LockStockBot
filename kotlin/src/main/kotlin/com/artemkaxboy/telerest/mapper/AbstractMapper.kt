package com.artemkaxboy.telerest.mapper

import com.artemkaxboy.telerest.dto.AbstractDto
import com.artemkaxboy.telerest.entity.AbstractEntity
import org.modelmapper.Converter
import org.modelmapper.ModelMapper
import org.modelmapper.spi.MappingContext

abstract class AbstractMapper<E : AbstractEntity, D : AbstractDto>(
    val mapper: ModelMapper,
    val entityClass: Class<E>,
    val dtoClass: Class<D>
) : Mapper<E, D> {

    override fun toEntity(dto: D?) = dto?.let { mapper.map(dto, entityClass) }

    override fun toDto(entity: E?) = entity?.let { mapper.map(entity, dtoClass) }

    fun dtoPostConverter(): Converter<E, D> {
        return Converter { context: MappingContext<E, D> ->
            postConvert(context.source, context.destination)
        }
    }

    open fun postConvert(source: E, destination: D) = destination

    fun entityPostConverter(): Converter<D, E> {
        return Converter { context: MappingContext<D, E> ->
            postConvert(context.source, context.destination)
        }
    }

    open fun postConvert(source: D, destination: E) = destination
}
