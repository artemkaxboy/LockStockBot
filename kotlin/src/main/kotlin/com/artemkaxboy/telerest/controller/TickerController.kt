package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.config.API_V1
import com.artemkaxboy.telerest.dto.ResponseDto
import com.artemkaxboy.telerest.service.forecast.impl.ForecastServiceImpl1
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import springfox.documentation.annotations.ApiIgnore

@RestController
@RequestMapping(value = ["api/$API_V1"])
@Api(tags = ["Ticker controller"], description = "Perform tickers operation")
class TickerController(
    private val forecastServiceImpl1: ForecastServiceImpl1
) {

    @GetMapping(
        "/tickers",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiOperation(value = "Get all tickers")
    @ApiResponses(value = [ApiResponse(code = 200, message = "OK")])
    private fun getUsers(
        // @RequestParam(required = false, defaultValue = "0")
        // page: Int,
        //
        // @RequestParam(required = false, defaultValue = "10")
        // pageSize: Int,

        @ApiIgnore
        request: ServerHttpRequest
    ): Mono<ResponseDto> {

        return ResponseDto
            .getResponse(request) {
                runBlocking {
                    forecastServiceImpl1.getBufferedFlow().toList()
                }
            }
            .toMono()
    }
}
