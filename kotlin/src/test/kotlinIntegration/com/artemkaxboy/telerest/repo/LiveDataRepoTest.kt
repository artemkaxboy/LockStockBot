package com.artemkaxboy.telerest.repo

import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.entity.LiveDataShallow
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
@DataJpaTest
internal class LiveDataRepoTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var liveDataRepo: LiveDataRepo

    @Test
    fun findByTicker_TickerAndDateTest() {

        val expected = liveDataRepo.save(LiveData.random())
        repeat((0..10).count()) {
            liveDataRepo.save(LiveData.random())
        }

        val actual = liveDataRepo.findByTicker_TickerAndDate(expected.ticker.ticker, expected.date)
        Assertions.assertThat(actual).isEqualTo(expected)

        val actualByClass = liveDataRepo.findByTicker_TickerAndDate(
            expected.ticker.ticker,
            expected.date,
            LiveData::class.java
        )
        Assertions.assertThat(actualByClass).isEqualTo(expected)

        val actualLiveDataShallow = liveDataRepo
            .findByTicker_TickerAndDate(expected.ticker.ticker, expected.date, LiveDataShallow::class.java)
        Assertions.assertThat(expected.equalsTo(actualLiveDataShallow)).isTrue()
    }

    @Test
    fun findByTicker_TickerAndDateBetweenOrderByDateTest() {
        val data = LiveData.random()
        val today = LocalDate.now()

        repeat((0..10).count()) {
            entityManager.persist(data.copy(date = today.minusDays(it.toLong())))
        }
        entityManager.flush()

        val week = liveDataRepo.findByTicker_TickerAndDateBetweenOrderByDate(
            data.ticker.ticker,
            LocalDate.now().minusDays(7),
            LocalDate.now().minusDays(1)
        )
        Assertions.assertThat(week.size).isEqualTo(7)

        val never = liveDataRepo.findByTicker_TickerAndDateBetweenOrderByDate(
            data.ticker.ticker,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(3)
        )
        Assertions.assertThat(never.size).isEqualTo(0)
    }
}
