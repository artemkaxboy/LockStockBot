package com.artemkaxboy.telerest.repo

import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.entity.LiveDataId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Suppress("FunctionName") // jpa search related names
@Repository
interface LiveDataRepo : JpaRepository<LiveData, LiveDataId> {

    /**
     * Finds given class projections of [LiveData] by ticker code and exact date.
     *
     * @return found entity or null if nothing found.
     */
    fun <T> findByTickerIdAndDate(ticker: String, date: LocalDate, clazz: Class<T>): T?

    /**
     * Finds given class projections of [LiveData] by ticker paged.
     *
     * @return page of found entities, empty page if nothing found.
     */
    fun <T> findByTickerId(ticker: String, pageable: Pageable, clazz: Class<T>): Page<T>

    /**
     * Finds [LiveData] by ticker code between given dates inclusively.
     *
     * @return list of found entities, empty list if nothing found.
     */
    fun findByTickerIdAndDateBetweenOrderByDate(
        ticker: String,
        fromInclusive: LocalDate,
        toInclusive: LocalDate
    ): MutableList<LiveData>

    /**
     * Finds all tickers [LiveData] by date paged.
     *
     * @return page of all available [LiveData] by date, empty page if nothing found.
     */
    fun findAllByDate(pageable: Pageable, date: LocalDate = LocalDate.now()): Page<LiveData>

    /**
     * Finds all tickers [LiveData] by date unpaged, sorted.
     *
     * @return list of all available [LiveData] by date, empty list if nothing found.
     */
    fun findAllByDate(sort: Sort, date: LocalDate = LocalDate.now()): List<LiveData>
}
