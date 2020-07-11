package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.config.API_V1
import com.artemkaxboy.telerest.dto.ResponseDto
import com.artemkaxboy.telerest.exception.RequestException
import com.artemkaxboy.telerest.mapper.LiveDataToLiveDataDtoMapper
import com.artemkaxboy.telerest.service.LiveDataService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.context.annotation.PropertySource
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
import springfox.documentation.annotations.ApiIgnore
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank


private
const val EDITABLE_DAYS_INTERVAL = 365L


@RestController @Validated
@RequestMapping(value = ["api/$API_V1"])
@Api(tags = ["Live data controller"], description = "Perform live data operation")
@PropertySource("classpath:swagger.properties")
class LiveDataController(
    private val liveDataService: LiveDataService,
    private val liveDataToLiveDataDtoMapper: LiveDataToLiveDataDtoMapper
) {

    @GetMapping("/liveData/{ticker}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "\${liveData.ticker.get}", response = ResponseDto::class)
    @ApiResponses(value = [ApiResponse(code = 200, message = "OK")])
    fun getLiveData(

        // @PropertySource doesn't work in validation annotation
        // https://docs.jboss.org/hibernate/validator/5.1/reference/en-US/html/chapter-message-interpolation.html#section-interpolation-with-message-expressions
        // https://howtodoinjava.com/hibernate/hibernate-validator-java-bean-validation/
        @NotBlank(message = "{invalid-ticker}")
        // @PropertySource doesn't work in @ApiParam example field
        @ApiParam(value = "\${liveData.ticker.get.ticker}", allowEmptyValue = false, example = "AMZN")
        @PathVariable
        ticker: String,

        @ApiIgnore
        request: ServerHttpRequest,

        @ApiIgnore
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
    @ApiOperation(value = "\${liveData.ticker.post}", response = ResponseDto::class)
    @ApiResponses(value = [ApiResponse(code = 200, message = "OK")])
    fun postTicker(

        @NotBlank(message = "{invalid-ticker}")
        @ApiParam(value = "\${liveData.ticker.post.ticker}", allowEmptyValue = false, example = "AMZN")
        @PathVariable
        ticker: String,

        @Min(-EDITABLE_DAYS_INTERVAL, message = "{liveData.ticker.post.days.min}")
        @Max(EDITABLE_DAYS_INTERVAL, message = "{liveData.ticker.post.days.max}")
        @ApiParam(
            value = "\${liveData.ticker.post.days}",
            allowableValues = "range[-365, 365]",
            example = "0"
        )
        @RequestParam(required = false)
        days: Int?,

        @ApiParam(
            value = "\${liveData.ticker.post.price}",
            example = "0.0"
        )
        @RequestParam(required = false)
        price: Double?,

        @ApiParam(
            value = "\${liveData.ticker.post.consensus}",
            example = "0.0"
        )
        @RequestParam(required = false)
        consensus: Double?,

        @ApiIgnore
        request: ServerHttpRequest
    ): Mono<ResponseDto> {

        return ResponseDto
            .getResponse(request) {
                liveDataService.postLiveData(ticker, days, price, consensus)
            }
            .toMono()
    }
}
