package com.artemkaxboy.telerest.repo

import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.entity.LiveDataShallow
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate
import kotlin.random.Random

@ExtendWith(SpringExtension::class)
@DataJpaTest
internal class LiveDataRepoTest {

    @Autowired
    private lateinit var liveDataRepo: LiveDataRepo

    @Test
    fun `fail to find tick by ticker and date`() {

        /* save a few tickers and get one of them randomly */
        val expected = (0..10)
            .map { LiveData.random() }
            .also { liveDataRepo.saveAll(it) }
            .random()

        /* find one by gotten id and date */
        val actual = liveDataRepo.findByTicker_IdAndDate(expected.ticker.id, expected.date)
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun `fail to find ticks projected by ticker and date`() {

        /* save a few tickers and get one of them randomly */
        val expected = (0..10)
            .map { LiveData.random() }
            .also { liveDataRepo.saveAll(it) }
            .random()

        /* find one by gotten id and date */
        val actualLiveData = liveDataRepo
            .findByTicker_IdAndDate(expected.ticker.id, expected.date, LiveData::class.java)
        Assertions.assertEquals(expected, actualLiveData)

        /* find one by gotten id and date */
        val actualLiveDataShallow = liveDataRepo
            .findByTicker_IdAndDate(expected.ticker.id, expected.date, LiveDataShallow::class.java)
        Assertions.assertTrue(expected.equalsTo(actualLiveDataShallow))
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
            .also { liveDataRepo.saveAll(it) }
            .sortedBy { it.date }

        /* save a few ticks of different tickers */
        (0..10)
            .map { LiveData.random() }
            .filter { it.ticker.id != liveData.ticker.id }
            .also { liveDataRepo.saveAll(it) }

        /* find all ticks by ticker */
        val pageable = PageRequest.of(0, 100, Sort.by(LiveData::date.name))
        val actual = liveDataRepo.findByTicker_Id(liveData.ticker.id, pageable, LiveData::class.java).content

        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun `fail to find ticks by ticker in date range`() {
        val liveData = LiveData.random()
        val today = LocalDate.now()

        /* save a row of ticks of one ticker for 11 days */
        val expected = (0..10L)
            .map { liveData.copy(date = today.minusDays(it)) }
            .also { liveDataRepo.saveAll(it) }
            .sortedBy { it.date }

        /* find last week ticks */
        val week = liveDataRepo.findByTicker_IdAndDateBetweenOrderByDate(
            liveData.ticker.id,
            today.minusDays(6),
            today
        )
        Assertions.assertEquals(expected.takeLast(7), week)

        /* find partly unavailable ticks */
        val four = liveDataRepo.findByTicker_IdAndDateBetweenOrderByDate(
            liveData.ticker.id,
            today.minusDays(3),
            today.plusDays(3)
        )
        Assertions.assertEquals(expected.takeLast(4), four)

        /* find unavailable ticks */
        val never = liveDataRepo.findByTicker_IdAndDateBetweenOrderByDate(
            liveData.ticker.id,
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
            .distinctBy { it.ticker.id }
            .also { liveDataRepo.saveAll(it) }
            .also { list ->
                liveDataRepo.saveAll(list.map { it.copy(date.minusDays(Random.nextLong(1, 365))) })
            }
            .also { list ->
                liveDataRepo.saveAll(list.map { it.copy(date.plusDays(Random.nextLong(1, 365))) })
            }
            .sortedBy { it.ticker.id }

        val sort = Sort.by(LiveData::ticker.name)

        /* find ticks of all tickers by date */
        val actual1 = liveDataRepo.findAllByDate(sort, date)
        Assertions.assertEquals(expected, actual1)

        /* find ticks of all tickers by date paged */
        val actual2 = liveDataRepo.findAllByDate(PageRequest.of(0, 100, sort), date).content
        Assertions.assertEquals(expected, actual2)
    }

    @Test
    fun `fail when findAllLatest returns wrong data`() {

        /* save 2 ticks for each of different random tickers, both have different random date */
        val expected = (0..10)
            .map { LiveData.random() }
            .distinctBy { it.ticker.id }
            .also { liveDataRepo.saveAll(it) }
            .map { it.copy(date = it.date.plusDays(Random.nextLong(10, 30))) }
            .also { liveDataRepo.saveAll(it) }
            .sortedBy { it.ticker.id }

        /* find latest ticks for all tickers */
        val actual = liveDataRepo.findAllLatest()
        Assertions.assertEquals(expected, actual)
    }

    @Test
    fun `fail to renew tick`() {

        val liveData = LiveData.random()

        /* save tick */
        val (unexpectedUpdated, unexpectedPrice) = liveDataRepo.saveAndFlush(liveData)
            .also { Assertions.assertNotNull(it.created) }
            .let { it.updated to it.price }

        /* update tick */
        val (actualUpdated, actualPrice) = liveDataRepo.saveAndFlush(liveData.copy(price = liveData.price * .9))
            .also { Assertions.assertNull(it.created) }
            .let { it.updated to it.price }

        /* check the difference */
        Assertions.assertNotEquals(unexpectedUpdated, actualUpdated)
        Assertions.assertNotEquals(unexpectedPrice, actualPrice)
    }
}
