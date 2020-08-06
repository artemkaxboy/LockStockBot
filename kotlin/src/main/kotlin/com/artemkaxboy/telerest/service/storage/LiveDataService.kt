package com.artemkaxboy.telerest.service.storage

import com.artemkaxboy.telerest.dto.LiveDataDto
import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.entity.LiveDataShallow
import com.artemkaxboy.telerest.mapper.LiveDataToLiveDataDtoMapper
import com.artemkaxboy.telerest.mapper.toDto
import com.artemkaxboy.telerest.repo.LiveDataRepo
import com.artemkaxboy.telerest.service.ConsensusService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

const val EDITABLE_DAYS_INTERVAL = 365L

private val defaultSorting = Sort.by(
    Sort.Order(Sort.Direction.DESC, LiveData::date.name),
    Sort.Order(Sort.Direction.ASC, LiveData::tickerId.name)
)

private val defaultPageRequest = PageRequest.of(0, 10, defaultSorting)

@Service
class LiveDataService(
    private val liveDataRepo: LiveDataRepo,
    private val tickerService: TickerService,
    private val forecastService: ForecastService,
    private val consensusService: ConsensusService
) {

    fun findByTickerIdAndDate(ticker: String, date: LocalDate) =
        liveDataRepo.findByTickerIdAndDate(ticker, date, LiveData::class.java)

    @Suppress("unused") // future
    fun <T> findByTickerIdAndDate(ticker: String, date: LocalDate, clazz: Class<T>) =
        liveDataRepo.findByTickerIdAndDate(ticker, date, clazz)

    fun findByTickerTickerAndDateBetweenOrderByDate(ticker: String, from: LocalDate, till: LocalDate) =
        liveDataRepo.findByTickerIdAndDateBetweenOrderByDate(ticker, from, till)

    fun findAllByDate(
        date: LocalDate = LocalDate.now(),
        pageRequest: Pageable = defaultPageRequest
    ): ResultData {
        if (pageRequest.isPaged)
            return ResultData.PagedResult(liveDataRepo.findAllByDate(pageRequest.defaultSortIfUnsorted(), date))

        return ResultData.UnpagedResult(liveDataRepo.findAllByDate(defaultSorting, date))
    }

    fun save(liveData: LiveData): LiveData {

        liveData.ticker?.also {
            tickerService.saveIfNotExist(it)
        }

        return liveDataRepo.save(liveData)
    }

    fun saveAll(list: List<LiveData>): List<LiveData> {

        list.mapNotNull { it.ticker }
            .distinctBy { it.id }
            .also { tickerService.saveAll(it) }

        return liveDataRepo.saveAll(list)
    }

    fun postLiveData(
        ticker: String,
        days: Int?,
        price: Double?,
        consensus: Double?
    ): LiveDataDto {

        val date = days?.takeIf { it != 0 }?.toLong()?.let { LocalDate.now().minusDays(it) }
            ?: LocalDate.now()

        val current = findByTickerIdAndDate(ticker, date)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Live data for ticker '$ticker' at $date not found.")

        val new = current
            .copy(
                price = price ?: current.price,
                consensus = consensus ?: current.consensus
            )

        return (new.takeIf { it != current }
            ?.let { save(it) }
            ?: current)
            .let { LiveDataToLiveDataDtoMapper.instance.toDto(it) }
    }

    fun findByTickerId(ticker: String, pageRequest: Pageable = defaultPageRequest) =
        liveDataRepo.findByTickerId(
            ticker,
            pageRequest.sortByDateDescIfUnsorted(),
            LiveDataShallow::class.java
        )
            .takeIf { it.totalElements > 0 }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Live data for ticker '$ticker' not found.")

    fun updateAndGet(tickerId: String, currentPrice: Double): LiveData {

        val today = LocalDate.now()
        val todayLiveData = findByTickerIdAndDate(tickerId, today)

        val newConsensus = forecastService.findAllUnexpiredByTickerId(tickerId)
            .let { consensusService.calculateConsensus(tickerId, it) }

        /* Save and return new LiveData if there is no today's one */
        todayLiveData
            ?: return LiveData(date = today, tickerId = tickerId, price = currentPrice, consensus = newConsensus)
                .also { save(it) }

        return todayLiveData.takeIf { it.price != currentPrice || it.consensus != newConsensus }
            ?.copy(price = currentPrice, consensus = newConsensus)
            ?: todayLiveData
    }

    /**
     * Deletes all entities in batch.
     *
     * @see JpaRepository.deleteAllInBatch
     */
    fun deleteAll() = liveDataRepo.deleteAllInBatch()
}

private fun Pageable.defaultSortIfUnsorted(): Pageable =
    this.takeIf { it.isPaged }
        ?.takeUnless { it.sort == Sort.unsorted() }
        ?: PageRequest.of(this.pageNumber, this.pageSize, defaultSorting)

private fun Pageable.sortByDateDescIfUnsorted(): Pageable =
    this.takeUnless { it.sort == Sort.unsorted() }
        ?: PageRequest.of(this.pageNumber, this.pageSize, Sort.Direction.DESC, LiveData::date.name)

// TODO use generic, encapsulate
sealed class ResultData {

    abstract fun getContent(): List<LiveData>

    data class PagedResult(val result: Page<LiveData>) : ResultData() {

        override fun getContent(): List<LiveData> = result.content
    }

    data class UnpagedResult(val result: List<LiveData>) : ResultData() {

        override fun getContent() = result
    }
}
