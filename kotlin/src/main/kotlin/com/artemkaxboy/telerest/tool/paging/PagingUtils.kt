package com.artemkaxboy.telerest.tool.paging

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl

inline fun <T, R> Page<T>.mapContent(block: (T) -> R): Page<R> =
    PageImpl(content.map(block), pageable, totalElements)
