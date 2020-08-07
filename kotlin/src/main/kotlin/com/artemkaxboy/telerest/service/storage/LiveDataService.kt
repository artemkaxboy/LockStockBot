package com.artemkaxboy.telerest.service.storage

import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.entity.LiveDataId
import com.artemkaxboy.telerest.entity.LiveDataShallow
import com.artemkaxboy.telerest.repo.LiveDataRepo
import com.artemkaxboy.telerest.tool.sorting.Sorting
import org.springframework.data.domain.Page
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
    fun findTodayDataByTickerId(tickerId: String): LiveData? =
        liveDataRepo.findByIdOrNull(LiveDataId(tickerId, LocalDate.now()))

    /**
     * Finds all [LiveData] by date.
     *
     * @return page of [LiveData].
     */
    fun findAllByDate(date: LocalDate = LocalDate.now(), pageable: Pageable = defaultPageRequest): Page<LiveData> {
        return liveDataRepo.findAllByDate(defaultSortIfUnsorted(pageable), date)
    }

    /**
     * Finds latest today's [LiveData] and merge it with changed values
     * or generate new [LiveData] if there is no today's one.
     */
    fun getLatestData(tickerId: String, currentPrice: Double): LiveData {

        val latestData = findTodayDataByTickerId(tickerId)
            ?: LiveData(tickerId = tickerId, price = Double.NaN)

        val newConsensus = forecastService.calculateConsensusByTickerId(tickerId)

        return latestData.takeIf { it.price != currentPrice || it.consensus != newConsensus }
            ?.copy(price = currentPrice, consensus = newConsensus)
            ?: latestData
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
}
