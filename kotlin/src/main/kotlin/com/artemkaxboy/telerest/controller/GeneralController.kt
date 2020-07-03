package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.config.API_V1
import com.artemkaxboy.telerest.config.properties.ApplicationProperties
import com.artemkaxboy.telerest.dto.ResponseDto
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import springfox.documentation.annotations.ApiIgnore

@Api(tags = ["General information"], description = "Get basic service information")
@RestController
@RequestMapping(value = ["api/$API_V1"])
class GeneralController(
    val applicationProperties: ApplicationProperties
) {

    @GetMapping("/version", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(value = "Get app version")
    @ApiResponses(value = [ApiResponse(code = 200, message = "OK")])
    private fun getVersion(@ApiIgnore request: ServerHttpRequest): Mono<ResponseDto> {
        return ResponseDto.getResponse(request) { applicationProperties.version }.toMono()
    }
}
