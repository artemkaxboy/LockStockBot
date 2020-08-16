package com.artemkaxboy.telerest.service.storage

import com.artemkaxboy.telerest.config.properties.ForecastSource1Properties
import com.artemkaxboy.telerest.entity.Forecast
import com.artemkaxboy.telerest.repo.ForecastRepo
import com.artemkaxboy.telerest.service.ConsensusService
import com.artemkaxboy.telerest.tool.ExceptionUtils.getMessage
import com.artemkaxboy.telerest.tool.Result
import com.artemkaxboy.telerest.tool.getOrElse
import com.artemkaxboy.telerest.tool.sorting.Sorting
import mu.KotlinLogging
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
    fun findAllUnexpiredByTickerId(tickerId: String): List<Forecast> {
        val error = "Cannot find unexpired forecasts (tickerId: $tickerId)"

        return Result
            .of(error) {
                forecastRepo.findLatestFromEachAnalystByTickerIdAndPublishDateAfterOrderByPublishDate(
                    tickerId,
                    LocalDateTime.now().minus(forecastTtl)
                )
            }
            .onFailure { logger.warn { it.getMessage() } }
            .getOrElse { emptyList() }
    }

    /**
     * Finds all forecast with given source id and upstream id list.
     */
    fun findAllBySourceIdAndUpstreamIdIn(sourceId: Int, upstreamIds: Iterable<String>): List<Forecast> {
        val error = "Cannot find forecasts (sourceId: $sourceId, upstreamIds: $upstreamIds)"

        return Result
            .of(error) { forecastRepo.findAllBySourceIdAndUpstreamIdIn(sourceId, upstreamIds) }
            .onFailure { logger.warn { it.getMessage() } }
            .getOrElse { emptyList() }
    }

    /**
     * Finds all forecasts for given source id and no analyst.
     */
    fun findAllBySourceIdAndAnalystNull(sourceId: Int): List<Forecast> {
        val error = "Cannot find forecasts (sourceId: $sourceId, analyst is null)"

        return Result
            .of(error) { forecastRepo.findAllBySourceIdAndAnalystNull(sourceId) }
            .onFailure { logger.warn { it.getMessage() } }
            .getOrElse { emptyList() }
    }

    /**
     * Saves entity to repo.
     *
     * @see CrudRepository.save
     */
    fun save(forecast: Forecast): Forecast? {
        val error = "Cannot save forecast (forecast: $forecast)"

        return Result.of(error) { forecastRepo.save(forecast) }
            .onFailure { logger.warn { it.getMessage() } }
            .getOrNull()
    }

    /**
     * Saves entities to repo.
     *
     * @see CrudRepository.saveAll
     */
    fun saveAll(forecasts: List<Forecast>): List<Forecast> {
        val error = "Cannot save forecasts"

        return Result
            .of(error) { forecastRepo.saveAll(forecasts) }
            .onFailure { logger.warn { it.getMessage() } }
            .getOrElse { emptyList() }
    }

    /**
     * Calculates consensus for given ticker id.
     *
     * @return consensus forecast or null, if it cannot be calculated.
     */
    fun calculateConsensusByTickerId(tickerId: String): Double? {

        val forecasts = findAllUnexpiredByTickerId(tickerId)

        return consensusService.calculateConsensus(tickerId, forecasts)
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
