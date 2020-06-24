package com.artemkaxboy.telerest.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Error", description = "Basic error information.")
data class ErrorDto(

    @ApiModelProperty(
        value = "Error code.",
        example = "1156",
        required = false
    )
    val code: Int? = null,

    @ApiModelProperty(
        value = "Error message.",
        example = "API error",
        required = false
    )
    val message: String? = null,

    @ApiModelProperty(
        value = "List of occurred errors.",
        required = false
    )
    val errors: List<ErrorDetailDto>? = null
)
