package com.artemkaxboy.telerest.service.storage

import com.artemkaxboy.telerest.config.properties.ForecastSource1Properties
import com.artemkaxboy.telerest.entity.Forecast
import com.artemkaxboy.telerest.repo.ForecastRepo
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

private val defaultSorting = Sort.by(Forecast::publishDate.name)

@Service
class ForecastService(
    private val forecastRepo: ForecastRepo,
    private val tickerService: TickerService,
    forecastSource1Properties: ForecastSource1Properties
) {

    private val forecastsTtl = forecastSource1Properties.ttl

    /**
     * Finds all entities by [Forecast.tickerId] which [Forecast.publishDate] age has not exceeded
     * [ForecastSource1Properties.ttl] and sorts them by [Forecast.publishDate].
     */
    fun findAllUnexpiredByTickerId(tickerId: String) =
        forecastRepo.findAllByTickerIdAndPublishDateAfter(
            tickerId,
            LocalDateTime.now().minus(forecastsTtl),
            defaultSorting
        )

    /**
     * Saves entities to repos transparently.
     *
     * @see CrudRepository.saveAll
     */
    fun saveAll(forecasts: List<Forecast>): List<Forecast> {

        forecasts.mapNotNull { it.ticker }
            .distinctBy { it.id }
            .also { tickerService.saveAll(it) }

        return forecastRepo.saveAll(forecasts)
    }

    /**
     * Deletes all entities in batch.
     *
     * @see JpaRepository.deleteAllInBatch
     */
    fun deleteAll() = forecastRepo.deleteAllInBatch()
}
