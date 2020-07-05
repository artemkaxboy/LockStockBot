package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.config.API_V1
import com.artemkaxboy.telerest.dto.ResponseDto
import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.entity.Ticker
import com.artemkaxboy.telerest.mapper.TickerMapper
import com.artemkaxboy.telerest.service.TickerService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import kotlinx.coroutines.runBlocking
import org.springframework.core.convert.ConversionService
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import springfox.documentation.annotations.ApiIgnore
import java.time.LocalDate

@RestController
@RequestMapping(value = ["api/$API_V1"])
@Api(tags = ["Ticker controller"], description = "Perform tickers operation")
class TickerController(
    private val tickerService: TickerService,
    private val conversionService: ConversionService,
    private val tickerMapper: TickerMapper
) {

    @GetMapping(
        "/tickers",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiOperation(value = "Get all tickers")
    @ApiResponses(value = [ApiResponse(code = 200, message = "OK")])
    private fun getTickers(

        @RequestParam(required = false, defaultValue = "0")
        page: Int,

        @RequestParam(required = false, defaultValue = "10")
        pageSize: Int,

        @ApiIgnore
        request: ServerHttpRequest
    ): Mono<ResponseDto> {

        return ResponseDto
            .getResponse(request) {
                tickerService.findAll(PageRequest.of(page, pageSize)).content.map { tickerMapper.toDto(it) }
            }
            .toMono()
    }

    @PostMapping(
        "/ticker",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiOperation(value = "Post ticker")
    @ApiResponses(value = [ApiResponse(code = 200, message = "OK")])
    private fun postTicker(

        @RequestParam(required = false, defaultValue = "0")
        days: Long,

        @RequestParam(required = false)
        price: Double?,

        @RequestParam(required = false)
        consensus: Double?,

        @ApiIgnore
        request: ServerHttpRequest
    ): Mono<ResponseDto> {

        val current = tickerService.findById("EQT")
        val newPrice = price ?: current?.liveData?.firstOrNull()?.price ?: 0.0
        val newConsensus = consensus ?: current?.liveData?.firstOrNull()?.consensus ?: 0.0

        val ticker = Ticker.EQT.apply {
            liveData = listOf(LiveData(LocalDate.now().plusDays(days), this, newPrice, newConsensus))
        }

        this.tickerService.save(conversionService.convert(ticker, Ticker::class.java))

        return ResponseDto
            .getResponse(request) {
                runBlocking {
                    // forecastServiceImpl1.getFlow().toList()
                }
            }
            .toMono()
    }
}
