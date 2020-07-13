package com.artemkaxboy.telerest.mapper

import com.artemkaxboy.telerest.dto.AbstractDto
import com.artemkaxboy.telerest.entity.AbstractEntity
import org.jetbrains.annotations.Contract

interface Mapper<E : AbstractEntity, D : AbstractDto> {

    @Contract("null -> null; !null -> !null")
    fun toEntity(dto: D?): E?

    @Contract("null -> null; !null -> !null")
    fun toDto(entity: E?): D?

    fun toDto(map: Map<*, *>): D?
}
