package com.artemkaxboy.telerest.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty


@ApiModel(value = "Data", description = "Requested data items or operation results.")
data class DataDto(

    @ApiModelProperty(
        value = "List of items or results.",
        required = true
    )
    val items: List<Any>,

    @ApiModelProperty(
        value = "The number of items in this result set. Should be equivalent to items.length, " +
            "and is provided as a convenience property. For example, " +
            "suppose a developer requests a set of search items, and asks for 10 items per page. " +
            "The total set of that search has 14 total items. The first page of items will have 10 items in it, " +
            "so both itemsPerPage and currentItemCount will equal \"10\". " +
            "The next page of items will have the remaining 4 items; itemsPerPage will still be \"10\", " +
            "but currentItemCount will be \"4\".",
        example = "4"
    )
    val currentItemCount: Int = 1,

    @ApiModelProperty(
        value = "The number of items in the result. This is not necessarily the size of the data.items array; " +
            "if we are viewing the last page of items, the size of data.items may be less than itemsPerPage. " +
            "However the size of data.items should not exceed itemsPerPage.",
        example = "10"
    )
    val itemsPerPage: Int = 1,

    @ApiModelProperty(
        value = "The index of the first item in data.items. For consistency, startIndex should be 1-based. " +
            "For example, the first item in the first set of items should have a startIndex of 1. " +
            "If the user requests the next set of data, the startIndex may be 10.",
        example = "20"
    )
    val startIndex: Long = 1,

    @ApiModelProperty(
        value = "The total number of items available in this set. For example, if a user has 100 blog posts, " +
            "the response may only contain 10 items, but the totalItems would be 100.",
        example = "100"
    )
    val totalItems: Long = 1,

    @ApiModelProperty(
        value = "The index of the current page of items. For consistency, pageIndex should be 1-based. " +
            "For example, the first page of items has a pageIndex of 1. " +
            "pageIndex can also be calculated from the item-based paging properties: " +
            "pageIndex = floor(startIndex / itemsPerPage) + 1.",
        example = "2"
    )
    val pageIndex: Int = 1,

    @ApiModelProperty(
        value = "The total number of pages in the result set. " +
            "totalPages can also be calculated from the item-based paging properties above: " +
            "totalPages = ceiling(totalItems / itemsPerPage).",
        example = "12"
    )
    val totalPages: Int = 1
) : AbstractDto
