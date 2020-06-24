package com.artemkaxboy.telerest.dto

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Error Detail", description = "Detailed error information.")
data class ErrorDetailDto(

    @ApiModelProperty(
        value = "Where the error come from.",
        example = "Telegram Bot"
    )
    val domain: String? = null,

    @ApiModelProperty(
        value = "Error reason.",
        example = "Cannot resolve DNS name"
    )
    val reason: String? = null,

    @ApiModelProperty(
        value = "Error message.",
        example = "Null pointer exception"
    )
    val message: String? = null,

    @ApiModelProperty(
        value = "Error location.",
        example = "35"
    )
    val location: String? = null,

    @ApiModelProperty(
        value = "Error location type.",
        example = "symbol"
    )
    val locationType: String? = null,

    @ApiModelProperty(
        value = "Extended help message.",
        example = "Set some property value. Documentation: https://tools.ietf.org/html/rfc3261"
    )
    val extendedHelp: String? = null
) {
    companion object {
        fun fromThrowable(
            throwable: Throwable,
            domain: String? = null
        ) = ErrorDetailDto(
            domain = domain,
            reason = throwable.cause?.message,
            message = throwable.message
        )
    }
}
