package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.entity.Ticker
import com.artemkaxboy.telerest.repo.TickerRepo
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class TickerService (private val tickerRepo: TickerRepo) {

    fun findAll(pageRequest: Pageable = defaultPageRequest) = tickerRepo.findAll(pageRequest.fixSorting())

    fun findById(id: String) = tickerRepo.findByIdOrNull(id)

    fun save(ticker: Ticker?) = ticker?.let { tickerRepo.save(ticker) }
}

private fun Pageable.fixSorting(): Pageable =
    this.takeUnless { it.sort == Sort.unsorted() }
        ?: PageRequest.of(this.pageNumber, this.pageSize, Sort.by(Ticker::name.name))

private val defaultPageRequest = PageRequest.of(0, 10)
