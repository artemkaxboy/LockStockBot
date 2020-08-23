package com.artemkaxboy.telerest.service.storage

import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.entity.LiveDataId
import com.artemkaxboy.telerest.entity.LiveDataShallow
import com.artemkaxboy.telerest.repo.LiveDataRepo
import com.artemkaxboy.telerest.tool.ExceptionUtils.getMessage
import com.artemkaxboy.telerest.tool.Result
import com.artemkaxboy.telerest.tool.sorting.Sorting
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

@Service
class LiveDataService(
    private val liveDataRepo: LiveDataRepo,
    private val forecastService: ForecastService
) : BaseStorageService(
    listOf(Sorting(LiveData::date, Sort.Direction.DESC), Sorting(LiveData::tickerId))
) {

    /**
     * Finds [LiveData] list by ticker id and date between given ones inclusively.
     */
    fun findByTickerIdAndDateBetweenOrderByDate(ticker: String, from: LocalDate, till: LocalDate): List<LiveData> =
        liveDataRepo.findByTickerIdAndDateBetweenOrderByDate(ticker, from, till)

    /**
     * Finds [LiveDataShallow] page by ticker id. todo try to get rid of shallow
     */
    fun findByTickerId(ticker: String, pageRequest: Pageable = defaultPageRequest): Page<LiveDataShallow> =
        liveDataRepo
            .findByTickerId(ticker, defaultSortIfUnsorted(pageRequest), LiveDataShallow::class.java)
            .takeIf { it.totalElements > 0 }
        // todo use result instead of throwing exception
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Live data for ticker '$ticker' not found.")

    /**
     * Finds today's [LiveData] by ticker id.
     */
    fun findTodayDataByTickerId(tickerId: String): LiveData? = findById(LiveDataId(tickerId, LocalDate.now()))

    /**
     * Finds yesterday's [LiveData] by ticker id.
     */
    fun findYesterdayDataByTickerId(tickerId: String): LiveData? =
        findById(LiveDataId(tickerId, LocalDate.now().minusDays(1)))

    /**
     * Retrieves an entity by its id. Never throws exception.
     *
     * @return the entity with the given id or null.
     */
    fun findById(id: LiveDataId): LiveData? {
        val error = "Cannot get live data (id: $id)"

        return Result.of(error) { liveDataRepo.findByIdOrNull(id) }
            .onFailure { logger.warn { it.getMessage() } }
            .getOrNull()
    }

    /**
     * Finds all [LiveData] by date. Filter out data without potential when potential-sorted.
     *
     * @return page of [LiveData].
     */
    fun findAllByDate(
        date: LocalDate = LocalDate.now(),
        pageable: Pageable = defaultPageRequest
    ): Result<Page<LiveData>> =
        Result.of("Cannot find LiveData by date") {
            if (pageable.sort.getOrderFor(LiveData::potential.name) != null) {
                liveDataRepo.findAllByDateAndPotentialNotNull(pageable, date)
            } else {
                liveDataRepo.findAllByDate(defaultSortIfUnsorted(pageable), date)
            }
        }

    /**
     * Finds today's data with given sort order.
     */
    fun findLiveData(
        order: Order,
        direction: Sort.Direction = Sort.Direction.ASC,
        pageable: Pageable = defaultPageRequest
    ): Result<Page<LiveData>> {
        val sortedPageRequest =
            PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.by(order.getSortOrder(direction)))

        return findAllByDate(LocalDate.now(), sortedPageRequest)
    }

    /**
     * Finds latest today's [LiveData] and merge it with changed values
     * or generate new [LiveData] if there is no today's one.
     */
    fun getLatestData(tickerId: String, currentPrice: Double): LiveData {

        val latestData = findTodayDataByTickerId(tickerId)
            ?: generateNewLatestData(tickerId)

        val newConsensus = forecastService.calculateConsensusByTickerId(tickerId)

        return latestData.takeIf { it.price != currentPrice || it.consensus != newConsensus }
            ?.copy(price = currentPrice, consensus = newConsensus)
            ?: latestData
    }

    private fun generateNewLatestData(tickerId: String): LiveData {

        return findYesterdayDataByTickerId(tickerId)
            ?.let { LiveData(tickerId = tickerId, previousPrice = it.price, previousConsensus = it.consensus) }
            ?: LiveData(tickerId = tickerId)
    }

    /**
     * Saves entity to repo.
     *
     * @see CrudRepository.save
     */
    fun save(liveData: LiveData): LiveData = liveDataRepo.save(liveData)

    /**
     * Saves entities to repo.
     *
     * @see CrudRepository.saveAll
     */
    fun saveAll(list: List<LiveData>): List<LiveData> {
        return liveDataRepo.saveAll(list)
    }

    @Suppress("unused") // used through reflection
    enum class Order(val field: String) {
        TICKER(LiveData::tickerId.name),
        POTENTIAL(LiveData::potential.name);

        fun getSortOrder(direction: Sort.Direction = Sort.Direction.ASC): Sort.Order =
            Sort.Order(direction, field)
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
