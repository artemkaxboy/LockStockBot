package com.artemkaxboy.telerest.service.storage

import com.artemkaxboy.telerest.entity.Forecast
import com.artemkaxboy.telerest.repo.ForecastRepo
import com.artemkaxboy.telerest.tool.Constants
import com.artemkaxboy.telerest.tool.RandomUtils
import com.artemkaxboy.telerest.tool.extensions.round
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // To make @BeforeAll non-static
internal class ForecastServiceTest {

    @Autowired
    lateinit var forecastService: ForecastService

    @Autowired
    lateinit var forecastRepo: ForecastRepo

    @Test
    fun `fail to find unexpired`() {
        val forecast = Forecast.random().copy(publishDate = LocalDateTime.now())

        /* save a few forecasts years-aged */
        (1..10L)
            .map {
                forecast.copy(
                    upstreamId = RandomUtils.forecastId(),
                    publishDate = forecast.publishDate.minusYears(it)
                )
            }
            .also { forecastService.saveAll(it) }

        /* save a few forecasts minutes-aged */
        val expected = (1..10L)
            .map {
                forecast.copy(
                    upstreamId = RandomUtils.forecastId(),
                    publishDate = forecast.publishDate.minusMinutes(it)
                )
            }
            .also { forecastService.saveAll(it) }
            .sortedWith(forecastService.defaultComparator)

        val actual = forecastService.findAllUnexpiredByTickerId(forecast.tickerId)
        assertEquals(expected, actual)
    }

    @Test
    fun `fail to save all`() {
        val initialCount = forecastRepo.findAll().size
        assertEquals(0, initialCount)

        val forecast = Forecast.random().copy(publishDate = LocalDateTime.now())
        val expected = (1..10L)
            .map {
                forecast.copy(
                    upstreamId = RandomUtils.forecastId(),
                    publishDate = forecast.publishDate.minusMinutes(it)
                )
            }
            .also { forecastService.saveAll(it) }
            .sortedWith(forecastService.defaultComparator)

        val actual = forecastService.findAllUnexpiredByTickerId(forecast.tickerId)
        assertEquals(expected, actual)
    }

    @Test
    fun `fail to calculate consensus`() {
        val forecast = Forecast.random().copy(publishDate = LocalDateTime.now())
        val expected = (1..10L)
            .map {
                forecast.copy(
                    upstreamId = RandomUtils.forecastId(),
                    targetPrice = forecast.targetPrice * 1.02
                )
            }
            .also { forecastService.saveAll(it) }
            .map { it.targetPrice }
            .average().round(Constants.PRICE_ROUND_PRECISION)

        val actual =
            forecastService.calculateConsensusByTickerId(forecast.tickerId)?.round(Constants.PRICE_ROUND_PRECISION)
        assertEquals(expected, actual)
    }

    @BeforeAll
    @AfterEach
    fun clean() {
        forecastRepo.deleteAll()
    }
}
