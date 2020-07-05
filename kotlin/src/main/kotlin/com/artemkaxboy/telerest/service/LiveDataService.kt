package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.repo.LiveDataRepo
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class LiveDataService(private val liveDataRepo: LiveDataRepo) {

    fun findFirstByTickerTickerOrderByDateDesc(ticker: String) =
        liveDataRepo.findFirstByTickerTickerOrderByDateDesc(ticker)

    fun findAllByDate(pageRequest: Pageable = defaultPageRequest, date: LocalDate = LocalDate.now()) =
        liveDataRepo.findAllByDate(pageRequest.fixSorting(), date)

    fun save(liveData: LiveData) = liveDataRepo.save(liveData)

    fun saveAll(list: List<LiveData?>): List<LiveData?> = liveDataRepo.saveAll(list)
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
