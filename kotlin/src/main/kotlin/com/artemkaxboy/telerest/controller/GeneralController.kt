package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.config.API_V1
import com.artemkaxboy.telerest.dto.ResponseDto
import com.artemkaxboy.telerest.service.GeneralService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.context.annotation.PropertySource
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
// @PropertySource doesn't work in the same-level @Api annotation
@Api(tags = ["General information"], description = "Get basic service information")
@PropertySource("classpath:swagger.properties")
class GeneralController(
    val generalService: GeneralService
) {

    @GetMapping("/version", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "\${version.get}", response = ResponseDto::class)
    @ApiResponses(value = [ApiResponse(code = 200, message = "OK")])
    fun getVersion(@ApiIgnore request: ServerHttpRequest): Mono<ResponseDto> {
        return ResponseDto.getResponse(request) { generalService.getVersion() }.toMono()
    }
}
