package com.artemkaxboy.telerest.mapper

import com.artemkaxboy.telerest.dto.AbstractDto
import com.artemkaxboy.telerest.entity.AbstractEntity

interface Mapper<E : AbstractEntity, D : AbstractDto> {

    fun toEntity(dto: D?): E?

    fun toDto(entity: E?): D?
}
