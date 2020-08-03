package com.artemkaxboy.telerest.repo

import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.entity.LiveDataId
import com.artemkaxboy.telerest.entity.Ticker
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
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
    fun <T> findByTicker_IdAndDate(ticker: String, date: LocalDate, clazz: Class<T>): T?

    /**
     * Finds [LiveData] by ticker code and date.
     *
     * @return found entity or null if nothing found.
     */
    fun findByTicker_IdAndDate(ticker: String, date: LocalDate): LiveData?

    /**
     * Finds given class projections of [LiveData] by ticker paged.
     *
     * @return page of found entities, empty page if nothing found.
     */
    fun <T> findByTicker_Id(ticker: String, pageable: Pageable, clazz: Class<T>): Page<T>

    /**
     * Finds [LiveData] by ticker code between given dates inclusively.
     *
     * @return list of found entities, empty list if nothing found.
     */
    fun findByTicker_IdAndDateBetweenOrderByDate(
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

    // @doc https://stackoverflow.com/questions/1313120/retrieving-the-last-record-in-each-group-mysql
    // Solution 1
    // SELECT * FROM live_data as l1 LEFT JOIN live_data as l2
    // ON (l1.ticker_fk = l2.ticker_fk AND l1.date < l2.date)
    // where l2.date is null ORDER BY l1.date DESC, l1.ticker_fk ASC
    /**
     * Finds latest [LiveData] for all available ticker.
     *
     * @return list of all available [Ticker]'s [LiveData] sorted by ticker code.
     */
    @Query(value = "FROM LiveData L1 LEFT JOIN LiveData L2 ON L1.ticker = L2.ticker AND L1.date < L2.date " +
        "WHERE L2.date IS NULL ORDER BY L1.ticker.id ASC")
    fun findAllLatest(): List<LiveData>
}
