package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.config.API_V1
import com.artemkaxboy.telerest.dto.ResponseDto
import com.artemkaxboy.telerest.mapper.TickerToTickerDtoMapper
import com.artemkaxboy.telerest.service.TickerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 * Maximum API supported value, bytes representation of 1GB.
 */
private const val MAX_API_INT = 1073741824L
private const val MAX_PAGE_SIZE = 100L
private const val DEFAULT_PAGE_SIZE = 10L

@RestController
@Validated
@RequestMapping(value = ["api/$API_V1"])
@Tag(name = "Ticker controller", description = "Perform tickers operation")
class TickerController(
    private val tickerService: TickerService,
    private val tickerToTickerDtoMapper: TickerToTickerDtoMapper
) {

    @GetMapping(
        "/tickers",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(summary = "Get all tickers")
    fun getTickers(

        @Min(1, message = "`page` must be integer number between 1 and $MAX_API_INT inclusively.")
        @Max(MAX_PAGE_SIZE, message = "`page` must be integer number between 1 and $MAX_API_INT inclusively.")
        @Schema(
            description = "Page number to get. Must be integer number between 1 and $MAX_API_INT inclusively.",
            required = false,
            minimum = "1"
        )
        @RequestParam(defaultValue = "1")
        page: Int,

        @Min(1, message = "`pageSize` must be positive integer number between 1 and $MAX_PAGE_SIZE inclusively.")
        @Max(
            MAX_PAGE_SIZE,
            message = "`pageSize` must be positive integer number between 1 and $MAX_PAGE_SIZE inclusively."
        )
        @Schema(
            description = "Page size to get. Must be integer number between 1 and $MAX_API_INT inclusively.",
            required = false,
            minimum = "1",
            maximum = "$MAX_PAGE_SIZE"
        )
        @RequestParam(defaultValue = "$DEFAULT_PAGE_SIZE")
        pageSize: Int,

        request: ServerHttpRequest
    ): Mono<ResponseDto> {

        return ResponseDto
            .getResponse(request) {

                tickerService.findAll(PageRequest.of(page - 1, pageSize)).let {
                    PageImpl(
                        it.content.map { e -> tickerToTickerDtoMapper.toDto(e) },
                        it.pageable,
                        it.totalElements
                    )
                }
            }
            .toMono()
    }
}
