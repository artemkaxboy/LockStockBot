package com.artemkaxboy.telerest.service.storage

import com.artemkaxboy.telerest.config.properties.ForecastSource1Properties
import com.artemkaxboy.telerest.entity.Forecast
import com.artemkaxboy.telerest.repo.ForecastRepo
import com.artemkaxboy.telerest.service.ConsensusService
import com.artemkaxboy.telerest.tool.sorting.Sorting
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ForecastService(
    private val forecastRepo: ForecastRepo,
    private val consensusService: ConsensusService,
    forecastSource1Properties: ForecastSource1Properties
) : BaseStorageService(
    listOf(Sorting(Forecast::publishDate))
) {

    private val forecastTtl = forecastSource1Properties.ttl

    /**
     * Finds all entities by [Forecast.tickerId] which [Forecast.publishDate] age has not exceeded
     * [ForecastSource1Properties.ttl], sorted by default with [Forecast.publishDate] ascending.
     */
    fun findAllUnexpiredByTickerId(tickerId: String): List<Forecast> =
        forecastRepo.findAllByTickerIdAndPublishDateAfter(
            tickerId,
            LocalDateTime.now().minus(forecastTtl),
            defaultSort
        )

    /**
     * Saves entities to repo.
     *
     * @see CrudRepository.saveAll
     */
    fun saveAll(forecasts: List<Forecast>): List<Forecast> = forecastRepo.saveAll(forecasts)

    /**
     * Calculates consensus for given ticker id.
     *
     * @return consensus forecast or null, if it cannot be calculated.
     */
    fun calculateConsensusByTickerId(tickerId: String): Double? =
        consensusService.calculateConsensus(tickerId, findAllUnexpiredByTickerId(tickerId))
}
