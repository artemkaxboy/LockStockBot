package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.config.API_V1
import com.artemkaxboy.telerest.dto.ResponseDto
import com.artemkaxboy.telerest.mapper.TickerToTickerDtoMapper
import com.artemkaxboy.telerest.service.TickerService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
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
import springfox.documentation.annotations.ApiIgnore
import javax.validation.constraints.Max
import javax.validation.constraints.Min


private
const val MAX_API_INT = 1073741824L


private
const val MAX_PAGE_SIZE = 100L


@RestController @Validated
@RequestMapping(value = ["api/$API_V1"])
@Api(tags = ["Ticker controller"], description = "Perform tickers operation")
class TickerController(
    private val tickerService: TickerService,
    private val tickerToTickerDtoMapper: TickerToTickerDtoMapper
) {

    @GetMapping(
        "/tickers",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiOperation(value = "Get all tickers", response = ResponseDto::class)
    @ApiResponses(value = [ApiResponse(code = 200, message = "OK")])
    fun getTickers(

        @Min(1, message = "Page number cannot be lower than 1.")
        @Max(MAX_API_INT, message = "Page number cannot be greater than $MAX_API_INT.")
        @ApiParam(allowableValues = "range[1, $MAX_API_INT]", defaultValue = "1")
        @RequestParam(required = false)
        page: Int,

        @Min(1, message = "Page size cannot be lower than 1.")
        @Max(MAX_PAGE_SIZE, message = "Page size cannot be greater than $MAX_PAGE_SIZE.")
        @ApiParam(allowableValues = "range[1, 100]", defaultValue = "10")
        @RequestParam(required = false)
        pageSize: Int,

        @ApiIgnore
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
