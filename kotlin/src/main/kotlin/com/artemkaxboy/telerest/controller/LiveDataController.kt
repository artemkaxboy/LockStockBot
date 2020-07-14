package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.config.API_V1
import com.artemkaxboy.telerest.dto.ResponseDto
import com.artemkaxboy.telerest.service.EDITABLE_DAYS_INTERVAL
import com.artemkaxboy.telerest.service.LiveDataService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
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
        request: ServerHttpRequest,

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
        pageSize: Int
    ): Mono<ResponseDto> {
        return ResponseDto.getResponse(request) {
            liveDataService.getLiveData(
                ticker,
                PageRequest.of(page - 1, pageSize)
            )
        }.toMono()
    }

    @PostMapping("/liveData/{ticker}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(
        summary = "Post ticker live data",
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
    fun postLiveData(
        request: ServerHttpRequest,

        @Parameter(description = "Ticker code, e.g. AAPL for Apple or AMZN for Amazon.", example = "AMZN")
        @PathVariable
        ticker: String,

        @Min(0)
        @Max(EDITABLE_DAYS_INTERVAL)
        @Parameter(
            description = "Days back to edit data. E.g. 0 - today, 1 - yesterday, 7 - week ago, 30 - month ago. " +
                "Must be an integer unsigned number between 0 and 365 inclusively.",
            example = "0"
        )
        @RequestParam(required = false)
        days: Int?,

        @Parameter(description = "Stock price to set.", example = "10.0")
        @RequestParam(required = false)
        price: Double?,

        @Parameter(description = "Consensus forecast to set.", example = "10.0")
        @RequestParam(required = false)
        consensus: Double?

    ): Mono<ResponseDto> {

        return ResponseDto.getResponse(request) { liveDataService.postLiveData(ticker, days, price, consensus) }
            .toMono()
    }
}
