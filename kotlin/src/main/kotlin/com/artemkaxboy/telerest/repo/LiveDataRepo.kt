package com.artemkaxboy.telerest.repo

import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.entity.LiveDataId
import java.time.LocalDate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Suppress("FunctionName") // jpa search related names
@Repository
interface LiveDataRepo : JpaRepository<LiveData, LiveDataId> {

    /**
     * @return latest [LiveData] for provided ticker if it exists, null - otherwise.
     */
    fun findFirstByTicker_TickerOrderByDateDesc(ticker: String): LiveData?

    fun findByTicker_TickerAndDate(ticker: String, date: LocalDate): LiveData?

    /**
     * @return page of all available [LiveData] by date.
     */
    fun findAllByDate(pageable: Pageable, date: LocalDate = LocalDate.now()): Page<LiveData>
}
