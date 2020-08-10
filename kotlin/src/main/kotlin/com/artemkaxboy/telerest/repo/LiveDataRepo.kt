package com.artemkaxboy.telerest.repo

import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.entity.LiveDataId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface LiveDataRepo : JpaRepository<LiveData, LiveDataId> {

    fun <T> findByTickerId(ticker: String, pageable: Pageable, clazz: Class<T>): Page<T>

    fun findByTickerIdAndDateBetweenOrderByDate(
        ticker: String,
        fromInclusive: LocalDate,
        toInclusive: LocalDate
    ): List<LiveData>

    fun findAllByDate(pageable: Pageable, date: LocalDate = LocalDate.now()): Page<LiveData>

    fun findAllByDateAndPotentialNotNull(pageable: Pageable, date: LocalDate = LocalDate.now()): Page<LiveData>
}
