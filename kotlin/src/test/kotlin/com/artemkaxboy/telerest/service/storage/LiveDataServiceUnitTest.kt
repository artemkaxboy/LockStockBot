package com.artemkaxboy.telerest.service.storage

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.data.domain.Sort

internal class LiveDataServiceUnitTest {

    @ParameterizedTest
    @EnumSource(Sort.Direction::class)
    fun `fail to get sort order`(direction: Sort.Direction) {

        LiveDataService.Order.values()
            .map { it to it.getSortOrder(direction) }
            .forEach {
                Assertions.assertEquals(it.first.field, it.second.property)
                Assertions.assertEquals(direction, it.second.direction)
            }
    }

    @Test
    fun `fail to get sort order with default direction`() {

        LiveDataService.Order.values()
            .map { it to it.getSortOrder() }
            .forEach {
                Assertions.assertEquals(it.first.field, it.second.property)
                Assertions.assertNotNull(it.second.direction)
            }
    }
}
