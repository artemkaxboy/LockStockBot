package com.artemkaxboy.telerest.service.storage

import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.entity.LiveDataId
import com.artemkaxboy.telerest.repo.LiveDataRepo
import com.artemkaxboy.telerest.tool.RandomUtils
import com.artemkaxboy.telerest.tool.paging.SinglePage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate
import kotlin.random.Random

@ExtendWith(SpringExtension::class)
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // To make @BeforeAll non-static
internal class LiveDataServiceTest {

    @Autowired
    private lateinit var liveDataService: LiveDataService

    @Autowired
    private lateinit var liveDataRepo: LiveDataRepo

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
            .sortedWith(liveDataService.defaultComparator)

        /* save a few ticks of different tickers */
        (0..10)
            .map { LiveData.random() }
            .filter { it.tickerId != liveData.tickerId }
            .also { liveDataService.saveAll(it) }

        /* find all ticks by ticker */
        val pageable = PageRequest.of(0, 100, liveDataService.defaultSort)
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
        val week = liveDataService.findByTickerIdAndDateBetweenOrderByDate(
            liveData.tickerId,
            today.minusDays(6),
            today
        )
        Assertions.assertEquals(expected.takeLast(7), week)

        /* find partly unavailable ticks */
        val four = liveDataService.findByTickerIdAndDateBetweenOrderByDate(
            liveData.tickerId,
            today.minusDays(3),
            today.plusDays(3)
        )
        Assertions.assertEquals(expected.takeLast(4), four)

        /* find unavailable ticks */
        val never = liveDataService.findByTickerIdAndDateBetweenOrderByDate(
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
            .sortedWith(liveDataService.defaultComparator)

        val actual1 = liveDataService
            .findAllByDate(date, SinglePage.of(liveDataService.defaultSort)).getOrNull()?.content
        Assertions.assertEquals(expected, actual1)
    }

    @Test
    fun `fail to renew tick`() {

        val liveData = LiveData.random()

        /* save tick */
        val (unexpectedUpdated, unexpectedPrice) = liveDataRepo.save(liveData)
            .also { Assertions.assertNotNull(it.created) }
            .let { it.updated to it.price }

        /* update tick */
        val (actualUpdated, actualPrice) = liveDataRepo.save(liveData.copy(price = liveData.price * .9))
            .also { Assertions.assertNull(it.created) }
            .let { it.updated to it.price }

        /* check the difference */
        Assertions.assertNotEquals(unexpectedUpdated, actualUpdated)
        Assertions.assertNotEquals(unexpectedPrice, actualPrice)
    }

    @Test
    fun `fail to filter no potential data on getting liveData sorted by potential`() {

        val maxCount = 10
        val (totalCount, consensusCount) = (1..maxCount)
            .map { counter ->
                LiveData.random(date = LocalDate.now(), consensus = RandomUtils.price().takeIf { counter % 2 == 1 })
            }
            .distinctBy { LiveDataId.of(it) }
            .also { liveDataRepo.saveAll(it) }
            .partition { it.consensus == null }
            .let { it.first.size + it.second.size to it.second.size }

        Assertions.assertNotEquals(0, consensusCount)
        Assertions.assertNotEquals(totalCount, consensusCount)

        val actualPotential = liveDataService.findLiveData(LiveDataService.Order.POTENTIAL).getOrNull()?.totalElements
        Assertions.assertEquals(consensusCount.toLong(), actualPotential)

        val actualCount = liveDataService.findLiveData(LiveDataService.Order.TICKER).getOrNull()?.totalElements
        Assertions.assertEquals(totalCount.toLong(), actualCount)
    }

    @AfterEach
    @BeforeAll
    fun clean() {
        liveDataRepo.deleteAll()
    }
}
