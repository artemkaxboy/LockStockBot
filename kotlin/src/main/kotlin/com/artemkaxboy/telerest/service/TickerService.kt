package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.entity.Ticker
import com.artemkaxboy.telerest.repo.TickerRepo
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class TickerService(private val tickerRepo: TickerRepo) {

    fun findAll(pageable: Pageable = defaultPageRequest) =
        tickerRepo.findAll(pageable.fixSorting())
}

private fun Pageable.fixSorting(): Pageable =
    this.takeUnless { it.sort == Sort.unsorted() }
        ?: PageRequest.of(
            this.pageNumber, this.pageSize, Sort.by(Ticker::ticker.name)
        )

private val defaultPageRequest = PageRequest.of(0, 10)
