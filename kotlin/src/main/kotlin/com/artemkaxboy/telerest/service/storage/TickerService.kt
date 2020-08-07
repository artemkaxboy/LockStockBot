package com.artemkaxboy.telerest.service.storage

import com.artemkaxboy.telerest.entity.Ticker
import com.artemkaxboy.telerest.repo.TickerRepo
import com.artemkaxboy.telerest.tool.sorting.Sorting
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
    fun findAll(pageRequest: Pageable = defaultPageRequest): Page<Ticker> =
        tickerRepo.findAll(defaultSortIfUnsorted(pageRequest))

    /**
     * Finds entity by id.
     *
     * @see CrudRepository.findByIdOrNull
     * @return found entity or null
     */
    fun findById(tickerId: String): Ticker? = tickerRepo.findByIdOrNull(tickerId)

    /**
     * Saves entity to repo.
     *
     * @see CrudRepository.save
     */
    fun save(ticker: Ticker): Ticker = tickerRepo.save(ticker)
}
