package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.config.API_V1
import com.artemkaxboy.telerest.controller.Constants.DEFAULT_PAGE_SIZE
import com.artemkaxboy.telerest.controller.Constants.MAX_API_INT
import com.artemkaxboy.telerest.controller.Constants.MAX_PAGE_SIZE
import com.artemkaxboy.telerest.dto.ResponseDto
import com.artemkaxboy.telerest.mapper.LiveDataToLiveDataDtoMapper
import com.artemkaxboy.telerest.mapper.toDto
import com.artemkaxboy.telerest.service.storage.LiveDataService
import com.artemkaxboy.telerest.tool.valueOfOrNull
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import javax.validation.constraints.Max
import javax.validation.constraints.Min

@RestController
@Validated
@RequestMapping(value = ["api/$API_V1"])
@Tag(name = "Live data controller", description = "Perform live data operation")
class LiveDataController(
    private val liveDataService: LiveDataService
) {

    @GetMapping("/liveData/{ticker}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Get ticker latest data",
        responses = [
            ApiResponse(responseCode = "202"),
            // code 400 does not extends common MediaType and Schema
            ApiResponse(
                responseCode = "400",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = ResponseDto::class)
                )]
            ),
            ApiResponse(responseCode = "404")
        ]
    )
    fun getLiveData(
        @Parameter(description = "Ticker code, e.g. AAPL for Apple or AMZN for Amazon.", example = "AMZN")
        @PathVariable
        ticker: String,

        @Min(1, message = "`page` must be integer number between 1 and $MAX_API_INT inclusively.")
        @Max(MAX_API_INT, message = "`page` must be integer number between 1 and $MAX_API_INT inclusively.")
        @Parameter(
            description = "Page number to get. Must be integer number between 1 and $MAX_API_INT inclusively.",
            example = "1",
            required = false
        )
        @RequestParam(defaultValue = "1")
        page: Int,

        @Min(1, message = "`pageSize` must be positive integer number between 1 and $MAX_PAGE_SIZE inclusively.")
        @Max(
            MAX_PAGE_SIZE,
            message = "`pageSize` must be positive integer number between 1 and $MAX_PAGE_SIZE inclusively."
        )
        @Parameter(
            description = "Page size to get. Must be integer number between 1 and $MAX_PAGE_SIZE inclusively.",
            example = "10",
            required = false
        )
        @RequestParam(defaultValue = "$DEFAULT_PAGE_SIZE")
        pageSize: Int,

        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Mono<ResponseDto> {
        return ResponseDto.getResponse(request, response) {
            liveDataService.findByTickerId(
                ticker,
                PageRequest.of(page - 1, pageSize)
            )
        }.toMono()
    }

    @GetMapping(
        "/liveData",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(summary = "Get all tickers live data.")
    fun getTickers(

        @Min(1, message = "`page` must be integer number between 1 and $MAX_API_INT inclusively.")
        @Max(MAX_API_INT, message = "`page` must be integer number between 1 and $MAX_API_INT inclusively.")
        @Parameter(
            description = "Page number to get. Must be integer number between 1 and $MAX_API_INT inclusively.",
            example = "1",
            required = false
        )
        @RequestParam(defaultValue = "1")
        page: Int,

        @Min(1, message = "`pageSize` must be positive integer number between 1 and $MAX_PAGE_SIZE inclusively.")
        @Max(
            MAX_PAGE_SIZE,
            message = "`pageSize` must be positive integer number between 1 and $MAX_PAGE_SIZE inclusively."
        )
        @Parameter(
            description = "Page size to get. Must be integer number between 1 and $MAX_PAGE_SIZE inclusively.",
            example = "10",
            required = false
        )
        @RequestParam(defaultValue = "$DEFAULT_PAGE_SIZE")
        pageSize: Int,

        @Parameter(
            description = "Data sort order. Allowed values: ticker, potential.",
            required = false,
            example = "ticker"
        )
        @RequestParam(defaultValue = "ticker")
        order: String,

        @Parameter(
            description = "Data sort direction. Allowed values: asc, desc.",
            required = false,
            example = "asc"
        )
        @RequestParam(defaultValue = "asc")
        direction: String,

        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): Mono<ResponseDto> {

        val directionValue = Sort.Direction::class.valueOfOrNull(direction)
            ?: return ResponseDto.wrapError(
                request,
                response,
                ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown direction value: $direction")
            ).toMono()

        val orderValue = LiveDataService.Order::class.valueOfOrNull(order)
            ?: return ResponseDto.wrapError(
                request,
                response,
                ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown order value: $order")
            ).toMono()

        val pageRequest = PageRequest.of(page - 1, pageSize)

        return ResponseDto
            .getResponse(request, response) {

                liveDataService
                    .findLiveData(orderValue, directionValue, pageRequest)
                    .map { LiveDataToLiveDataDtoMapper.instance.toDto(it) }
            }
            .toMono()
    }
}
