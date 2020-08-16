package com.artemkaxboy.telerest.repo

import com.artemkaxboy.telerest.entity.Forecast
import com.artemkaxboy.telerest.entity.ForecastId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ForecastRepo : JpaRepository<Forecast, ForecastId> {

    // https://docs.jboss.org/hibernate/stable/core.old/reference/en/html/queryhql-subqueries.html
    // SELECT f1.analyst, f1.ticker_id, f1.target_price, f1.publish_date
    // FROM forecasts f1
    //     INNER JOIN (
    //         SELECT MAX(f2.publish_date) AS max_date, f2.analyst, f2.ticker_id
    //         FROM forecasts f2
    //         WHERE f2.publish_date > make_date(2020, 7, 15)
    //         GROUP BY f2.analyst, f2.ticker_id) f2
    //     ON f1.analyst = f2.analyst AND f1.ticker_id = f2.ticker_id AND f1.publish_date = f2.max_date
    // ORDER BY f1.ticker_id, f1.analyst;
    @Query("FROM Forecast f1 WHERE f1.tickerId = :tickerId AND f1.publishDate > :date AND f1.publishDate = (SELECT MAX(f2.publishDate) FROM Forecast f2 WHERE f2.analyst = f1.analyst AND f2.tickerId = f1.tickerId) ORDER BY f1.publishDate")
    fun findLatestFromEachAnalystByTickerIdAndPublishDateAfterOrderByPublishDate(
        tickerId: String,
        date: LocalDateTime
    ): List<Forecast>

    fun findAllBySourceIdAndUpstreamIdIn(sourceId: Int, upstreamIds: Iterable<String>): List<Forecast>

    fun findAllBySourceIdAndAnalystNull(sourceId: Int): List<Forecast>
}
