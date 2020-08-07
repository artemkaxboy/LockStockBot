package com.artemkaxboy.telerest.tool.sorting

import org.springframework.data.domain.Sort
import kotlin.reflect.KProperty1

class Sorting(

    val field: KProperty1<out Any, Any>,

    val direction: Sort.Direction = Sort.Direction.ASC
) {

    companion object {

        fun getSort(sorting: List<Sorting>): Sort {

            return sorting
                .map { Sort.Order(it.direction, it.field.name) }
                .map { Sort.by(it) }
                .reduce { acc, sort -> acc.and(sort) }
        }

        fun getComparator(sorting: List<Sorting>): Comparator<Any> {

            return sorting
                .map { sortingItem ->
                    @Suppress("UNCHECKED_CAST") // no other way
                    compareBy(sortingItem.field.getter as (Any) -> Comparable<Any>)
                        .let { it.takeIf { sortingItem.direction == Sort.Direction.DESC }?.reversed() ?: it }
                }
                .reduce { acc, comparator -> acc.thenComparing(comparator) }
        }
    }
}
