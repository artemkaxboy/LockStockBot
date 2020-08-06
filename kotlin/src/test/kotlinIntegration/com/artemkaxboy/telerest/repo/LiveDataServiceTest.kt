package com.artemkaxboy.telerest.repo

import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.entity.LiveDataShallow
import com.artemkaxboy.telerest.service.storage.LiveDataService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate
import kotlin.random.Random

@ExtendWith(SpringExtension::class)
@SpringBootTest
internal class LiveDataServiceTest {

    @Autowired
    private lateinit var liveDataService: LiveDataService

    @Test
    fun `fail to find tick by ticker and date`() {

        /* save a few tickers and get one of them randomly */
        val expected = (0..10)
            .map { LiveData.random() }
            .also { liveDataService.saveAll(it) }
            .random()

        /* find one by gotten id and date */
        val actual = liveDataService.findByTickerIdAndDate(expected.tickerId, expected.date)
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun `fail to find ticks projected by ticker and date`() {

        /* save a few tickers and get one of them randomly */
        val expected = (0..10)
            .map { LiveData.random() }
            .also { liveDataService.saveAll(it) }
            .random()

        /* find one by gotten id and date */
        val actualLiveData = liveDataService
            .findByTickerIdAndDate(expected.tickerId, expected.date, LiveData::class.java)
        Assertions.assertEquals(expected, actualLiveData)

        /* find one by gotten id and date */
        val actualLiveDataShallow = liveDataService
            .findByTickerIdAndDate(expected.tickerId, expected.date, LiveDataShallow::class.java)
        Assertions.assertTrue(expected.equalTo(actualLiveDataShallow))
    }

    @Test
    fun `fail to find all ticks by ticker`() {

        val liveData = LiveData.random()
        val date = liveData.date

        /* save a few ticks of one ticker */
        val expected = (0..10)
            .map { Random.nextLong(1, 365) }
            .distinct()
            .map { liveData.copy(date = date.minusDays(it)) }
            .also { liveDataService.saveAll(it) }
            .sortedBy { it.date }

        /* save a few ticks of different tickers */
        (0..10)
            .map { LiveData.random() }
            .filter { it.tickerId != liveData.tickerId }
            .also { liveDataService.saveAll(it) }

        /* find all ticks by ticker */
        val pageable = PageRequest.of(0, 100, Sort.by(LiveData::date.name))
        val actual = liveDataService.findByTickerId(liveData.tickerId, pageable).content

        Assertions.assertEquals(expected.size, actual.size)
        expected.zip(actual)
            .forEach { Assertions.assertTrue(it.first.equalTo(it.second)) }
    }

    @Test
    fun `fail to find ticks by ticker in date range`() {
        val liveData = LiveData.random()
        val today = LocalDate.now()

        /* save a row of ticks of one ticker for 11 days */
        val expected = (0..10L)
            .map { liveData.copy(date = today.minusDays(it)) }
            .also { liveDataService.saveAll(it) }
            .sortedBy { it.date }

        /* find last week ticks */
        val week = liveDataService.findByTickerTickerAndDateBetweenOrderByDate(
            liveData.tickerId,
            today.minusDays(6),
            today
        )
        Assertions.assertEquals(expected.takeLast(7), week)

        /* find partly unavailable ticks */
        val four = liveDataService.findByTickerTickerAndDateBetweenOrderByDate(
            liveData.tickerId,
            today.minusDays(3),
            today.plusDays(3)
        )
        Assertions.assertEquals(expected.takeLast(4), four)

        /* find unavailable ticks */
        val never = liveDataService.findByTickerTickerAndDateBetweenOrderByDate(
            liveData.tickerId,
            today.plusDays(1),
            today.plusDays(3)
        )
        Assertions.assertTrue(never.isEmpty())
    }

    @Test
    fun `fail to find all ticks by date`() {
        val date = LocalDate.now().minusDays(Random.nextLong(100, 365))

        /* save 3 ticks for each of different random tickers, one for exact date, another before it, third after it */
        val expected = (0..10)
            .map { LiveData.random().copy(date = date) }
            .distinctBy { it.tickerId }
            .also { liveDataService.saveAll(it) }
            .also { list ->
                liveDataService.saveAll(list.map { it.copy(date.minusDays(Random.nextLong(1, 365))) })
            }
            .also { list ->
                liveDataService.saveAll(list.map { it.copy(date.plusDays(Random.nextLong(1, 365))) })
            }
            .sortedBy { it.tickerId }

        val sort = Sort.by(LiveData::tickerId.name)

        /* find ticks of all tickers by date */
        val actual1 = liveDataService.findAllByDate(date, Pageable.unpaged()).getContent()
        Assertions.assertEquals(expected, actual1)

        /* find ticks of all tickers by date paged */
        val actual2 = liveDataService.findAllByDate(date, PageRequest.of(0, 100, sort)).getContent()
        Assertions.assertEquals(expected, actual2)
    }

    @Test
    fun `fail to renew tick`() {

        val liveData = LiveData.random()

        /* save tick */
        val (unexpectedUpdated, unexpectedPrice) = liveDataService.save(liveData)
            .also { Assertions.assertNotNull(it.created) }
            .let { it.updated to it.price }

        /* update tick */
        val (actualUpdated, actualPrice) = liveDataService.save(liveData.copy(price = liveData.price * .9))
            .also { Assertions.assertNull(it.created) }
            .let { it.updated to it.price }

        /* check the difference */
        Assertions.assertNotEquals(unexpectedUpdated, actualUpdated)
        Assertions.assertNotEquals(unexpectedPrice, actualPrice)
    }

    @AfterEach
    fun clean() {
        liveDataService.deleteAll()
    }
}
