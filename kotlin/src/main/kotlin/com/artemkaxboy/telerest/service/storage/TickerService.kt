package com.artemkaxboy.telerest.service.storage

import com.artemkaxboy.telerest.entity.Ticker
import com.artemkaxboy.telerest.repo.TickerRepo
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

private val defaultSorting = Sort.by(Ticker::id.name)

private val defaultPageRequest = PageRequest.of(0, 10)

@Service
class TickerService(
    private val tickerRepo: TickerRepo,
    private val currencyService: CurrencyService
) {

    /**
     * Finds all available tickers, pageable.
     */
    fun findAll(pageRequest: Pageable = defaultPageRequest): TickerResultData {

        if (pageRequest.isPaged) {
            return tickerRepo
                .findAll(pageRequest.defaultSortIfUnsorted())
                .let { TickerResultData.PagedTickerResult(it) }
        }

        return tickerRepo.findAll(defaultSorting)
            .let { TickerResultData.UnpagedTickerResult(it) }
    }

    fun findById(tickerId: String): Ticker? {
        return tickerRepo.findByIdOrNull(tickerId)
    }

    fun saveIfChanged(ticker: Ticker): Ticker {
        return ticker
            .takeIf { it != tickerRepo.findByIdOrNull(ticker.id) }
            ?.let { tickerRepo.save(it) }
            ?: ticker
    }

    fun save(ticker: Ticker): Ticker {
        currencyService.saveIfNotExist(ticker.currency)
        return tickerRepo.save(ticker)
    }

    fun saveIfNotExist(ticker: Ticker): Ticker {
        return ticker
            .takeUnless { tickerRepo.existsById(ticker.id) }
            ?.let { save(it) }
            ?: ticker
    }

    fun saveAll(list: List<Ticker>): List<Ticker> {

        list.map { it.currency }
            .distinctBy { it.id }
            .also { currencyService.saveAll(it) }

        return tickerRepo.saveAll(list)
    }

    /**
     * Deletes all entities in batch.
     *
     * @see JpaRepository.deleteAllInBatch
     */
    fun deleteAll() = tickerRepo.deleteAllInBatch()
}

private fun Pageable.defaultSortIfUnsorted(): Pageable =
    this.takeIf { it.sort != Sort.unsorted() }
        ?: PageRequest.of(this.pageNumber, this.pageSize, defaultSorting)

sealed class TickerResultData {

    abstract fun getContent(): List<Ticker>
    abstract fun getPageable(): Pageable
    abstract fun getTotalElements(): Long

    data class PagedTickerResult(val result: Page<Ticker>) : TickerResultData() {

        override fun getContent(): List<Ticker> = result.content
        override fun getPageable() = result.pageable
        override fun getTotalElements() = result.totalElements
    }

    data class UnpagedTickerResult(val result: List<Ticker>) : TickerResultData() {

        override fun getContent() = result
        override fun getPageable() = Pageable.unpaged()
        override fun getTotalElements() = result.size.toLong()
    }
}
