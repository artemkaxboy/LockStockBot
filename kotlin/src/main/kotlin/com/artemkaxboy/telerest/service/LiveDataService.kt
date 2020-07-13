package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.dto.LiveDataDto
import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.mapper.LiveDataToLiveDataDtoMapper
import com.artemkaxboy.telerest.repo.LiveDataRepo
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

const val EDITABLE_DAYS_INTERVAL = 365L

@Service
data class LiveDataService(
    private val liveDataRepo: LiveDataRepo,
    private val liveDataToLiveDataDtoMapper: LiveDataToLiveDataDtoMapper
) {

    fun findFirstByTickerTickerOrderByDateDesc(ticker: String) =
        liveDataRepo.findFirstByTicker_TickerOrderByDateDesc(ticker)

    fun findByTickerTickerAndDate(ticker: String, date: LocalDate) =
        liveDataRepo.findByTicker_TickerAndDate(ticker, date)

    fun findAllByDate(pageRequest: Pageable = defaultPageRequest, date: LocalDate = LocalDate.now()) =
        liveDataRepo.findAllByDate(pageRequest.fixSorting(), date)

    fun save(liveData: LiveData) = liveDataRepo.save(liveData)

    fun saveAll(list: List<LiveData?>): List<LiveData?> = liveDataRepo.saveAll(list)

    fun postLiveData(
        ticker: String,
        days: Int?,
        price: Double?,
        consensus: Double?
    ): LiveDataDto {

        val date = days?.takeIf { it != 0 }?.toLong()?.let { LocalDate.now().minusDays(it) }
            ?: LocalDate.now()

        val current = findByTickerTickerAndDate(ticker, date)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Live data for ticker '$ticker' at $date not found.")

        val new = current
            .copy(
                price = price ?: current.price,
                consensus = consensus ?: current.consensus
            )

        return (new.takeIf { it != current }
            ?.let { save(it) }
            ?: current)
            .let { liveDataToLiveDataDtoMapper.toDto(it) }
            .let { requireNotNull(it) }
    }

    fun getLiveData(ticker: String): LiveDataDto {
        return findFirstByTickerTickerOrderByDateDesc(ticker)
            ?.let { liveDataToLiveDataDtoMapper.toDto(it) }
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Ticker '$ticker' not found.")
    }
}

private fun Pageable.fixSorting(): Pageable =
    this.takeUnless { it.sort == Sort.unsorted() }
        ?: PageRequest.of(
            this.pageNumber, this.pageSize, Sort.by(
                Sort.Order(Sort.Direction.DESC, LiveData::date.name),
                Sort.Order(Sort.Direction.ASC, LiveData::ticker.name)
            )
        )

private val defaultPageRequest = PageRequest.of(0, 10)
