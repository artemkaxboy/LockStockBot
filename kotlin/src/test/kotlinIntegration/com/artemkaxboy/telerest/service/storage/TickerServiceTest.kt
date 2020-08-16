package com.artemkaxboy.telerest.service.storage

import com.artemkaxboy.telerest.entity.Forecast
import com.artemkaxboy.telerest.entity.Ticker
import com.artemkaxboy.telerest.repo.CurrencyRepo
import com.artemkaxboy.telerest.repo.ForecastRepo
import com.artemkaxboy.telerest.repo.TickerRepo
import com.artemkaxboy.telerest.tool.RandomUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest
internal class TickerServiceTest {

    @Autowired
    lateinit var tickerService: TickerService

    @Autowired
    lateinit var tickerRepo: TickerRepo

    @Autowired
    lateinit var currencyRepo: CurrencyRepo

    @Autowired
    lateinit var forecastRepo: ForecastRepo

    @Test
    fun `fail to find all`() {
        val expected = listOf(Ticker.random(), Ticker.random(), Ticker.random(), Ticker.random())
            .distinctBy { it.id }
            .also { tickerRepo.saveAll(it) }
            .sortedBy { it.id }

        val actual = tickerService.findAll().content
        assertEquals(expected, actual)
    }

    @Test
    fun `fail to find id`() {
        val expected = listOf(Ticker.random(), Ticker.random(), Ticker.random(), Ticker.random())
            .distinctBy { it.id }
            .also { tickerRepo.saveAll(it) }
            .random()

        val actual = tickerService.findById(expected.id)
        assertEquals(expected, actual)
    }

    @Test
    fun `fail to save`() {
        val expected = Ticker.random()
            .also { tickerService.save(it) }

        val actual = tickerService.findAll().content
            .also { assertEquals(1, it.size) }
            .first()

        assertEquals(expected, actual)
    }

    // TODO check orphans
    @Test
    fun `fail to delete all`() {
        val expected = listOf(Ticker.random(), Ticker.random(), Ticker.random(), Ticker.random())
            .distinctBy { it.id }
            .also { tickerRepo.saveAll(it) }
            .random()

        tickerRepo.deleteAll()

        val actualSize = tickerService.findAll().totalElements
        assertEquals(0, actualSize)

        val actualCurrency = currencyRepo.findByIdOrNull(expected.currencyId)
        assertEquals(expected.currency, actualCurrency)
    }

    @Test
    fun `pass if saving ticker with new forecasts only doesn't affect previous`() {
        val ticker = Ticker.random()
        val forecast = Forecast.random(ticker.id).copy(publishDate = LocalDateTime.now())
        val expected = 2L

        val oldForecasts = (1..expected)
            .map {
                val id = RandomUtils.forecastId()
                forecast.copy(
                    upstreamId = id,
                    analyst = id
                )
            }
            .also { tickerService.save(ticker.copy(forecasts = it)) }
            .take(expected.toInt() / 2)

        val actual = forecastRepo.count()
        assertEquals(expected, actual)

        (1..expected)
            .map {
                val id = RandomUtils.forecastId()
                forecast.copy(
                    upstreamId = id,
                    analyst = id
                )
            }
            .plus(oldForecasts)
            .let { ticker.copy(forecasts = it) }
            .also { tickerService.save(it) }

        val actual2 = forecastRepo.count()
        assertEquals(expected * 2, actual2)
    }

    @Test
    fun `pass if saving ticker with old and new forecasts doesn't duplicate data`() {
        val ticker = Ticker.random()
        val forecast = Forecast.random(ticker.id).copy(publishDate = LocalDateTime.now())
        val expected = 2L

        (1..expected)
            .map {
                val id = RandomUtils.forecastId()
                forecast.copy(
                    upstreamId = id,
                    analyst = id
                )
            }
            .also { tickerService.save(ticker.copy(forecasts = it)) }

        val actual = forecastRepo.count()
        assertEquals(expected, actual)

        (1..expected)
            .map {
                val id = RandomUtils.forecastId()
                forecast.copy(
                    upstreamId = id,
                    analyst = id
                )
            }
            .let { ticker.copy(forecasts = it) }
            .also { tickerService.save(it) }

        val actual2 = forecastRepo.count()
        assertEquals(expected * 2, actual2)
    }

    @AfterEach
    fun clean() {
        tickerRepo.deleteAll()
    }
}
