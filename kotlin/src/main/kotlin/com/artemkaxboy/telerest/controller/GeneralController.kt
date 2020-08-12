package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.config.API_V1
import com.artemkaxboy.telerest.dto.ResponseDto
import com.artemkaxboy.telerest.service.GeneralService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@RestController
@RequestMapping(value = ["api/$API_V1"])
@Tag(name = "General information", description = "Get basic service information")
class GeneralController(
    val generalService: GeneralService
) {

    @GetMapping("/version", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Get app version", description = "Get app version information.")
    fun getVersion(request: ServerHttpRequest, response: ServerHttpResponse): Mono<ResponseDto> {
        return ResponseDto.getResponse(request, response) { generalService.getVersion() }.toMono()
    }
}
