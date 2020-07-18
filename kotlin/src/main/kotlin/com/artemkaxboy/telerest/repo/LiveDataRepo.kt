package com.artemkaxboy.telerest.repo

import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.entity.LiveDataId
import java.time.LocalDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Suppress("FunctionName") // jpa search related names
@Repository
interface LiveDataRepo : JpaRepository<LiveData, LiveDataId> {

    fun <T> findByTicker_TickerAndDate(ticker: String, date: LocalDate, clazz: Class<T>?): T?

    fun <T> findByTicker_Ticker(ticker: String, pageable: Pageable, clazz: Class<T>): Page<T>

    fun findByTicker_TickerAndDate(ticker: String, date: LocalDate): LiveData?

    /**
     * @return page of all available [LiveData] by date.
     */
    fun findAllByDate(pageable: Pageable, date: LocalDate = LocalDate.now()): Page<LiveData>

    fun findAllByDate(sort: Sort, date: LocalDate = LocalDate.now()): List<LiveData?>

    // @doc https://stackoverflow.com/questions/1313120/retrieving-the-last-record-in-each-group-mysql
    // Solution 1
    // SELECT * FROM live_data as l1 LEFT JOIN live_data as l2
    // ON (l1.ticker_fk = l2.ticker_fk AND l1.date < l2.date)
    // where l2.date is null ORDER BY l1.date DESC, l1.ticker_fk ASC

    @Query(value = "FROM LiveData L1 LEFT JOIN LiveData L2 ON L1.ticker = L2.ticker AND L1.date < L2.date WHERE L2.date IS NULL ORDER BY L1.ticker.ticker ASC")
    fun findAllLatest(): List<LiveData?>
}
