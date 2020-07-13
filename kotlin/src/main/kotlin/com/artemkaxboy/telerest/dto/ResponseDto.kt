package com.artemkaxboy.telerest.dto

import com.artemkaxboy.telerest.config.CURRENT_API_VERSION
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.server.ResponseStatusException

// based on https://google.github.io/styleguide/jsoncstyleguide.xml
@Schema(title = "Response", description = "Object contains request information, operation result or occurred errors.")
data class ResponseDto(

    // @ApiModelProperty(
    //     value = "Used API version.",
    //     example = "v1",
    //     required = false
    // )
    val apiVersion: String = CURRENT_API_VERSION,

    // @ApiModelProperty(
    //     value = "Unique request ID.",
    //     example = "33a2eec1-2",
    //     required = false
    // )
    val id: String? = null,

    // @ApiModelProperty(
    //     value = "Requested method.",
    //     example = "/api/v1/message",
    //     required = false
    // )
    val method: String? = null,

    // @ApiModelProperty(
    //     value = "Occurred error or null.",
    //     example = "to-do",
    //     required = false
    // )
    val data: DataDto? = null,

    // @ApiModelProperty(
    //     value = "Occurred error or null.",
    //     example = "to-do",
    //     required = false
    // )
    val error: ErrorDto? = null
) : AbstractDto {

    companion object {

        /**
         * Performs given [block] and wraps result into [ResponseDto].[DataDto] object.
         *
         * @return filled [ResponseDto]
         */
        fun getResponse(
            request: ServerHttpRequest,
            block: () -> Any
        ): ResponseDto {

            return wrapData(request, block())
        }

        fun wrapError(
            throwable: Throwable,
            request: ServerHttpRequest
        ): ResponseDto {
            val errorCode = ((throwable as? ResponseStatusException)?.status ?: HttpStatus.INTERNAL_SERVER_ERROR)
                .value()

            val message = when (throwable) {
                is ResponseStatusException ->
                    throwable.reason
                else ->
                    throwable.localizedMessage ?: throwable.toString()
            }

            return ErrorDto(
                errorCode,
                message,
                errors = ErrorDetailDto.fromThrowable(throwable)
            )
                .let {
                    ResponseDto(
                        id = request.id,
                        method = request.path.value(),
                        error = it
                    )
                }
        }

        private fun wrapData(
            request: ServerHttpRequest,
            result: Any
        ): ResponseDto {

            val data = when (result) {
                is Page<*> ->
                    DataDto(
                        items = result.content,
                        currentItemCount = result.count(),
                        itemsPerPage = result.size,
                        startIndex = result.pageable.offset + 1,
                        totalItems = result.totalElements,
                        pageIndex = result.number + 1,
                        totalPages = result.totalPages
                    )
                else -> DataDto(listOf(result))
            }

            return ResponseDto(
                id = request.id,
                method = request.path.value(),
                data = data
            )
        }
    }
}
