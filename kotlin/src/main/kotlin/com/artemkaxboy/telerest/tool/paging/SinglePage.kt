package com.artemkaxboy.telerest.tool.paging

import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

class SinglePage private constructor(private val sort: Sort) : Pageable {

    override fun getPageNumber(): Int = 0

    override fun hasPrevious(): Boolean = false

    override fun getSort(): Sort = sort

    override fun next(): Pageable = this

    override fun getPageSize(): Int = Int.MAX_VALUE

    override fun getOffset(): Long = 0

    override fun first(): Pageable = this

    override fun previousOrFirst(): Pageable = this

    companion object {

        fun unsorted(): SinglePage =
            SinglePage(Sort.unsorted())

        fun of(sort: Sort): SinglePage =
            SinglePage(sort)
    }
}
