package com.artemkaxboy.telerest.dto

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.web.server.ResponseStatusException

@Schema(title = "Error Detail", description = "Detailed error information.")
data class ErrorDetailDto(

    // @ApiModelProperty(
    //     value = "Where the error come from.",
    //     example = "Telegram Bot"
    // )
    val domain: String? = null,

    // @ApiModelProperty(
    //     value = "Error reason.",
    //     example = "Cannot resolve DNS name"
    // )
    val reason: String? = null,

    // @ApiModelProperty(
    //     value = "Error message.",
    //     example = "Null pointer exception"
    // )
    val message: String? = null,

    // @ApiModelProperty(
    //     value = "Error location.",
    //     example = "35"
    // )
    val location: String? = null,

    // @ApiModelProperty(
    //     value = "Error location type.",
    //     example = "symbol"
    // )
    val locationType: String? = null,

    // @ApiModelProperty(
    //     value = "Extended help message.",
    //     example = "Set some property value. Documentation: https://tools.ietf.org/html/rfc3261"
    // )
    val extendedHelp: String? = null
) : AbstractDto {

    companion object {

        /**
         * Extracts error fields from throwable to place it to [ErrorDetailDto].
         *
         * @return filled [ErrorDetailDto] object.
         */
        fun fromThrowable(
            throwable: Throwable,
            domain: String? = null
        ): List<ErrorDetailDto> {
            val result = mutableListOf<ErrorDetailDto>()

            var anotherThrowable: Throwable? = throwable
            do {
                result.add(fromOneThrowable(anotherThrowable!!, domain))
                anotherThrowable = anotherThrowable.cause?.takeIf { it !== anotherThrowable }
            } while (anotherThrowable != null)

            return result
        }

        private fun fromOneThrowable(
            throwable: Throwable,
            domain: String? = null
        ): ErrorDetailDto {

            val reason = when (throwable) {
                is ResponseStatusException ->
                    throwable.reason
                else ->
                    throwable.cause?.message
            }

            return ErrorDetailDto(
                domain = domain,
                reason = reason,
                message = throwable.message ?: throwable.toString()
            )
        }
    }
}
