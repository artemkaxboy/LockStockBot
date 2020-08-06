package com.artemkaxboy.telerest.repo

import com.artemkaxboy.telerest.entity.Forecast
import com.artemkaxboy.telerest.entity.ForecastId
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ForecastRepo : JpaRepository<Forecast, ForecastId> {

    fun findAllByTickerIdAndPublishDateAfter(tickerId: String, date: LocalDateTime, sort: Sort): List<Forecast>
}
