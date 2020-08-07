package com.artemkaxboy.telerest.service.storage

import com.artemkaxboy.telerest.tool.sorting.Sorting
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

private const val DEFAULT_PAGE_SIZE = 10

open class BaseStorageService(
    defaultSortFields: List<Sorting>,
    defaultPage: Pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE)
) {

    val defaultSort = Sorting.getSort(defaultSortFields)

    val defaultComparator = Sorting.getComparator(defaultSortFields)

    protected val defaultPageRequest = PageRequest.of(defaultPage.pageNumber, defaultPage.pageSize, defaultSort)

    protected fun defaultSortIfUnsorted(pageable: Pageable): Pageable {
        require(pageable.isPaged)
        return pageable.takeIf { it.sort.isUnsorted }
            ?.let { PageRequest.of(it.pageNumber, it.pageSize, defaultSort) }
            ?: pageable
    }
}
