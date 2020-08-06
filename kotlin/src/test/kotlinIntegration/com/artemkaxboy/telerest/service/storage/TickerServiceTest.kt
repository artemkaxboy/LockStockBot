package com.artemkaxboy.telerest.service.storage

import com.artemkaxboy.telerest.entity.Ticker
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
internal class TickerServiceTest {

    @Autowired
    lateinit var tickerService: TickerService

    @Autowired
    lateinit var currencyService: CurrencyService

    @Test
    fun `fail to find all`() {
        val expected = listOf(Ticker.random(), Ticker.random(), Ticker.random(), Ticker.random())
            .distinctBy { it.id }
            .also { tickerService.saveAll(it) }
            .sortedBy { it.id }

        val actual = tickerService.findAll().getContent()
        assertEquals(expected, actual)
    }

    @Test
    fun `fail to find id`() {
        val expected = listOf(Ticker.random(), Ticker.random(), Ticker.random(), Ticker.random())
            .distinctBy { it.id }
            .also { tickerService.saveAll(it) }
            .random()

        val actual = tickerService.findById(expected.id)
        assertEquals(expected, actual)
    }

    @Test
    fun `fail to save changed`() {
        val unexpected = Ticker.random()
            .let { tickerService.save(it) }

        val expected = unexpected
            .copy(url = "https://newsite.com/")
            .let { tickerService.saveIfChanged(it) }

        val actual = tickerService.findById(unexpected.id)

        assertNotEquals(unexpected, actual)
        assertEquals(expected, actual)
        assertNotEquals(unexpected.updated, actual?.updated)
    }

    @Test
    fun `fail to prevent saving unchanged`() {
        val expected = Ticker.random()
            .let { tickerService.save(it) }

        val actualSaved = tickerService.saveIfChanged(expected)

        assertSame(expected, actualSaved)

        val actual = tickerService.findById(expected.id)

        assertEquals(expected, actual)
        assertEquals(expected.updated, actual?.updated)
    }

    @Test
    fun `fail to save`() {
        val expected = Ticker.random()
            .also { tickerService.save(it) }

        val actual = tickerService.findAll().getContent()
            .also { assertEquals(1, it.size) }
            .first()

        assertEquals(expected, actual)
    }

    @Test
    fun `fail to save nonexistent`() {
        val expected = Ticker.random()
            .let { tickerService.saveIfNotExist(it) }

        val actual = tickerService.findById(expected.id)

        assertEquals(expected, actual)
    }

    @Test
    fun `fail to prevent saving existed`() {
        val expected = Ticker.random()
            .let { tickerService.saveIfNotExist(it) }

        val unexpected = expected
            .copy(url = "https://newsite.com/")
            .let { tickerService.saveIfNotExist(it) }

        val actual = tickerService.findById(expected.id)

        assertNotEquals(unexpected, actual)
        assertEquals(expected, actual)
        assertEquals(expected.updated, actual?.updated)
    }

    @Test
    // FIXME flaky
    fun `fail to save all`() {
        val expected = listOf(Ticker.random(), Ticker.random(), Ticker.random(), Ticker.random())
            .distinctBy { it.id }
            .also { tickerService.saveAll(it) }
            .sortedBy { it.id }

        val actual = tickerService.findAll().getContent()
        assertEquals(expected, actual)
    }

    @Test
    fun `fail to delete all`() {
        val expectedCurrency = listOf(Ticker.random(), Ticker.random(), Ticker.random(), Ticker.random())
            .distinctBy { it.id }
            .also { tickerService.saveAll(it) }
            .random().currency

        tickerService.deleteAll()

        val actualSize = tickerService.findAll().getContent().size
        assertEquals(0, actualSize)

        val actualCurrency = currencyService.findById(expectedCurrency.id)
        assertEquals(expectedCurrency, actualCurrency)
    }

    @AfterEach
    fun clean() {
        tickerService.deleteAll()
    }
}
