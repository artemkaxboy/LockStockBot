package com.artemkaxboy.telerest.service.storage

import com.artemkaxboy.telerest.entity.Ticker
import com.artemkaxboy.telerest.repo.TickerRepo
import com.artemkaxboy.telerest.tool.ExceptionUtils.getMessage
import com.artemkaxboy.telerest.tool.Result
import com.artemkaxboy.telerest.tool.sorting.Sorting
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class TickerService(
    private val tickerRepo: TickerRepo
) : BaseStorageService(
    listOf(Sorting(Ticker::id))
) {

    /**
     * Finds all available entities, pageable.
     *
     * @see PagingAndSortingRepository.findAll
     */
    fun findAll(pageRequest: Pageable = defaultPageRequest): Page<Ticker>? {
        val error = "Cannot find tickers"
        return Result
            .of(error) { tickerRepo.findAll(defaultSortIfUnsorted(pageRequest)) }
            .onFailure { logger.warn { it.getMessage() } }
            .getOrNull()
    }

    /**
     * Finds entity by id.
     *
     * @see CrudRepository.findByIdOrNull
     * @return found entity or null
     */
    fun findById(tickerId: String): Ticker? {
        val error = "Cannot find ticker $tickerId"
        return Result
            .of(error) { tickerRepo.findByIdOrNull(tickerId) }
            .onFailure { logger.warn { it.getMessage() } }
            .getOrNull()
    }

    /**
     * Saves entity to repo.
     *
     * @see CrudRepository.save
     */
    fun save(ticker: Ticker): Ticker? {
        val error = "Cannot save ticker ${ticker.id}"
        return Result
            .of(error) { tickerRepo.save(ticker) }
            .onFailure { logger.warn { it.getMessage() } }
            .getOrNull()
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
