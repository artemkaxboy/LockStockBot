package com.artemkaxboy.telerest.dto

import com.artemkaxboy.telerest.config.CURRENT_API_VERSION
import com.artemkaxboy.telerest.exception.RequestException
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse

// based on https://google.github.io/styleguide/jsoncstyleguide.xml
@ApiModel(value = "Response", description = "Object contains request information, operation result or occurred errors.")
data class ResponseDto(

    @ApiModelProperty(
        value = "Used API version.",
        example = "v1",
        required = false
    )
    val apiVersion: String = CURRENT_API_VERSION,

    @ApiModelProperty(
        value = "Unique request ID.",
        example = "33a2eec1-2",
        required = false
    )
    val id: String? = null,

    @ApiModelProperty(
        value = "Requested method.",
        example = "/api/v1/message",
        required = false
    )
    val method: String? = null,

    @ApiModelProperty(
        value = "Occurred error or null.",
        example = "todo",
        required = false
    )
    val data: DataDto? = null,

    @ApiModelProperty(
        value = "Occurred error or null.",
        example = "todo",
        required = false
    )
    val error: ErrorDto? = null
) : AbstractDto {

    companion object {

        /**
         * Performs given [block] and wraps result into [ResponseDto].[DataDto] object.
         * If [block] threw any exception wraps it into [ResponseDto].[ErrorDto] object.
         *
         * @return filled [ResponseDto]
         */
        fun getResponse(
            request: ServerHttpRequest,
            response: ServerHttpResponse? = null,
            block: () -> Any
        ): ResponseDto {

            return runCatching { wrap(request, block()) }
                .getOrElse { wrapError(request, response, it) }
        }

        private fun wrapError(
            request: ServerHttpRequest,
            response: ServerHttpResponse? = null,
            error: Throwable
        ): ResponseDto {
            val errorCode = ((error as? RequestException)?.code ?: HttpStatus.INTERNAL_SERVER_ERROR)
                .also { response?.statusCode = it }
                .value()

            return ErrorDto(
                errorCode,
                error.message ?: error.toString(),
                errors = ErrorDetailDto.fromThrowable(error)
            )
                .let {
                    ResponseDto(
                        id = request.id,
                        method = request.path.value(),
                        error = it
                    )
                }
        }

        private fun wrap(
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
