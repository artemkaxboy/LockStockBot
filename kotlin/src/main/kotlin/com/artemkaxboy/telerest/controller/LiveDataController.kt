package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.config.API_V1
import com.artemkaxboy.telerest.dto.ResponseDto
import com.artemkaxboy.telerest.exception.RequestException
import com.artemkaxboy.telerest.mapper.LiveDataToLiveDataDtoMapper
import com.artemkaxboy.telerest.service.LiveDataService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

private const val EDITABLE_DAYS_INTERVAL = 365L

@RestController
@Validated
@RequestMapping(value = ["api/$API_V1"])
@Tag(name = "Live data controller", description = "Perform live data operation")
class LiveDataController(
    private val liveDataService: LiveDataService,
    private val liveDataToLiveDataDtoMapper: LiveDataToLiveDataDtoMapper
) {

    @GetMapping("/liveData/{ticker}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Get ticker latest data")
    fun getLiveData(

        // https://docs.jboss.org/hibernate/validator/5.1/reference/en-US/html/chapter-message-interpolation.html#section-interpolation-with-message-expressions
        // https://howtodoinjava.com/hibernate/hibernate-validator-java-bean-validation/
        @NotBlank(message = "{invalid-ticker}")
        @Parameter(description = "Ticker code, e.g. AAPL for Apple or AMZN for Amazon.", example = "AMZN")
        @PathVariable
        ticker: String,

        request: ServerHttpRequest,

        response: ServerHttpResponse
    ): Mono<ResponseDto> {

        return ResponseDto
            .getResponse(request, response) {
                liveDataService.findFirstByTickerTickerOrderByDateDesc(ticker)
                    ?.let { liveDataToLiveDataDtoMapper.toDto(it) }
                    ?: throw RequestException("Ticker '$ticker' not found", HttpStatus.NOT_FOUND)
            }
            .toMono()
    }

    @PostMapping("/liveData/{ticker}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Post ticker live data", responses = [ApiResponse(responseCode = "202")])
    fun postTicker(

        @NotBlank(message = "{invalid-ticker}")
        @Parameter(description = "Ticker code, e.g. AAPL for Apple or AMZN for Amazon.", example = "AMZN")
        @PathVariable
        ticker: String,

        @Min(-EDITABLE_DAYS_INTERVAL, message = "{liveData.ticker.post.days.min}")
        @Max(EDITABLE_DAYS_INTERVAL, message = "{liveData.ticker.post.days.max}")
        @Schema(
            description = "Signed days diff from today to edit values. " +
                "Must be an integer signed number between -365 and 365 inclusively.",
            minimum = "-$EDITABLE_DAYS_INTERVAL",
            maximum = "$EDITABLE_DAYS_INTERVAL",
            example = "0"
        )
        @RequestParam(required = false)
        days: Int?,

        @Schema(description = "Stock price to set.", minimum = "0.0", example = "10.0")
        @RequestParam(required = false)
        price: Double?,

        @Schema(description = "Consensus forecast to set.", minimum = "0.0", example = "10.0")
        @RequestParam(required = false)
        consensus: Double?,

        request: ServerHttpRequest
    ): Mono<ResponseDto> {

        return ResponseDto
            .getResponse(request) {
                liveDataService.postLiveData(ticker, days, price, consensus)
            }
            .toMono()
    }
}
