package com.artemkaxboy.telerest.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Data", description = "Requested data items or operation results.")
data class DataDto(

    @ApiModelProperty(
        value = "List of items or results.",
        required = true
    )
    val items: List<Any>
)
