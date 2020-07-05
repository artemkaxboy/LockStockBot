package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.config.API_V1
import com.artemkaxboy.telerest.dto.ResponseDto
import com.artemkaxboy.telerest.mapper.LiveDataToLiveDataDtoMapper
import com.artemkaxboy.telerest.service.LiveDataService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import springfox.documentation.annotations.ApiIgnore

@RestController
@RequestMapping(value = ["api/$API_V1"])
@Api(tags = ["Ticker controller"], description = "Perform tickers operation")
class TickerController(
    private val liveDataService: LiveDataService,
    private val liveDataToLiveDataDtoMapper: LiveDataToLiveDataDtoMapper
) {

    @GetMapping(
        "/tickers",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiOperation(value = "Get all tickers", response = ResponseDto::class)
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
                liveDataService.findAllByDate(PageRequest.of(page, pageSize)).content
                    .map { liveDataToLiveDataDtoMapper.toDto(it) }
            }
            .toMono()
    }
}
