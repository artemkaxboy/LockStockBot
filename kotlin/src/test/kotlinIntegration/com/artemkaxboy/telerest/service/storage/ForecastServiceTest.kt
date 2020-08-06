package com.artemkaxboy.telerest.service.storage

import com.artemkaxboy.telerest.entity.Forecast
import com.artemkaxboy.telerest.tool.RandomUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest
internal class ForecastServiceTest {

    @Autowired
    lateinit var forecastService: ForecastService

    @Test
    fun findAllUnexpiredByTickerIdTest() {
        val forecast = Forecast.random().copy(publishDate = LocalDateTime.now())
        val expected = (1..10L)
            .map {
                forecast.copy(
                    upstreamId = RandomUtils.forecastId(),
                    publishDate = forecast.publishDate.minusMinutes(it)
                )
            }
            .also { forecastService.saveAll(it) }
            .sortedBy { it.publishDate }

        val actual = forecastService.findAllUnexpiredByTickerId(forecast.tickerId)
        assertEquals(expected, actual)
    }

    @Test
    fun `fail to save all`() {
        val forecast = Forecast.random().copy(publishDate = LocalDateTime.now())
        val expected = (1..10L)
            .map {
                forecast.copy(
                    upstreamId = RandomUtils.forecastId(),
                    publishDate = forecast.publishDate.minusMinutes(it)
                )
            }
            .also { forecastService.saveAll(it) }
            .sortedBy { it.publishDate }

        val actual = forecastService.findAllUnexpiredByTickerId(forecast.tickerId)
        assertEquals(expected, actual)
    }

    @Test
    fun deleteAllTest() {
        val forecast = Forecast.random().copy(publishDate = LocalDateTime.now())
        val expectedSize = (1..10L)
            .map {
                forecast.copy(
                    upstreamId = RandomUtils.forecastId(),
                    publishDate = forecast.publishDate.minusMinutes(it)
                )
            }
            .let { forecastService.saveAll(it) }
            .size
        val foundSize = forecastService.findAllUnexpiredByTickerId(forecast.tickerId).size

        assertNotEquals(0, foundSize)
        assertEquals(expectedSize, foundSize)

        forecastService.deleteAll()

        val foundSize2 = forecastService.findAllUnexpiredByTickerId(forecast.tickerId).size
        assertEquals(0, foundSize2)
    }

    @AfterEach
    fun clean() {
        forecastService.deleteAll()
    }
}
