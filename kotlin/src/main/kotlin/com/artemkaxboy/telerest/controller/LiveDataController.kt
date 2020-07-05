package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.config.API_V1
import com.artemkaxboy.telerest.dto.LiveDataDto
import com.artemkaxboy.telerest.dto.ResponseDto
import com.artemkaxboy.telerest.mapper.LiveDataToLiveDataDtoMapper
import com.artemkaxboy.telerest.service.LiveDataService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import springfox.documentation.annotations.ApiIgnore

@RestController
@RequestMapping(value = ["api/$API_V1"])
@Api(tags = ["Live data controller"], description = "Perform live data operation")
class LiveDataController(
    private val liveDataService: LiveDataService,
    private val liveDataToLiveDataDtoMapper: LiveDataToLiveDataDtoMapper
) {

    @GetMapping(
        "/liveData/{ticker}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiOperation(value = "Get ticker latest data", response = ResponseDto::class)
    @ApiResponses(value = [ApiResponse(code = 200, message = "OK")])
    private fun getLiveData(

        @PathVariable
        ticker: String,

        @ApiIgnore
        request: ServerHttpRequest
    ): Mono<ResponseDto> {

        return ResponseDto
            .getResponse(request) {
                liveDataService.findFirstByTickerTickerOrderByDateDesc(ticker)
                    ?.let { liveDataToLiveDataDtoMapper.toDto(it) }
                    ?: throw RuntimeException("Cannot find any data for ticker: $ticker")
            }
            .toMono()
    }

    @PostMapping(
        "/liveData/{ticker}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiOperation(value = "Post ticker live data", response = ResponseDto::class)
    @ApiResponses(value = [ApiResponse(code = 200, message = "OK")])
    private fun postTickerW(

        @PathVariable
        ticker: String,

        @RequestParam(required = false, defaultValue = "0")
        days: Long,

        @RequestParam(required = false)
        price: Double?,

        @RequestParam(required = false)
        consensus: Double?,

        @ApiIgnore
        request: ServerHttpRequest
    ): Mono<ResponseDto> {

        return ResponseDto
            .getResponse(request) {
                postTicker(ticker, days, price, consensus)
                // runBlocking {
                //     forecastServiceImpl1.getFlow().toList()
                // }
            }
            .toMono()
    }

    private fun postTicker(ticker: String, days: Long, price: Double?, consensus: Double?): LiveDataDto {
        val current = liveDataService.findFirstByTickerTickerOrderByDateDesc("EQT")
            ?: throw RuntimeException("Cannot find any data for ticker: $ticker")

        days.takeIf { it != 0L }?.also { current.date = current.date.plusDays(it) }
        price?.also { current.price = it }
        consensus?.also { current.consensus = it }

        return liveDataService.save(current)
            .let { liveDataToLiveDataDtoMapper.toDto(it) }
            .let { requireNotNull(it) }
    }
}
