package com.artemkaxboy.telerest.mapper

import com.artemkaxboy.telerest.dto.AbstractDto
import com.artemkaxboy.telerest.entity.AbstractEntity
import org.modelmapper.Converter
import org.modelmapper.ModelMapper
import org.modelmapper.spi.MappingContext

abstract class AbstractMapper<E : AbstractEntity, D : AbstractDto>(
    val mapper: ModelMapper
) {

    fun entityPostConverter(): Converter<D, E> {
        return Converter { context: MappingContext<D, E> ->
            postConvert(context.source, context.destination)
        }
    }

    open fun postConvert(source: D, destination: E) = destination

    fun dtoPostConverter(): Converter<E, D> {
        return Converter { context: MappingContext<E, D> ->
            postConvert(context.source, context.destination)
        }
    }

    open fun postConvert(source: E, destination: D) = destination
}

inline fun <reified E : AbstractEntity, D : AbstractDto> AbstractMapper<E, D>.toEntity(dto: D): E =
    mapper.map(dto, E::class.java)

inline fun <E : AbstractEntity, reified D : AbstractDto> AbstractMapper<E, D>.toDto(entity: E): D =
    mapper.map(entity, D::class.java)

inline fun <reified E : AbstractEntity, reified D : AbstractDto> AbstractMapper<E, D>.createTypeMaps() {

    mapper.createTypeMap(D::class.java, E::class.java).apply {
        setPostConverter(entityPostConverter())
    }

    mapper.createTypeMap(E::class.java, D::class.java).apply {
        setPostConverter(dtoPostConverter())
    }
}
