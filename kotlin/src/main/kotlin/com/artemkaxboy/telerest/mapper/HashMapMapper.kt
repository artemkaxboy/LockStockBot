package com.artemkaxboy.telerest.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class HashMapMapper(private val objectMapper: ObjectMapper) {

    fun <T : Any> mapToObject(map: Map<*, *>, clazz: KClass<T>): T {
        return objectMapper.readValue(objectMapper.writeValueAsString(map), clazz.java)
    }
}
