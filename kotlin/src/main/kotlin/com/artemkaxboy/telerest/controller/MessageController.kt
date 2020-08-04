package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.config.API_V1
import com.artemkaxboy.telerest.dto.MessageDto
import com.artemkaxboy.telerest.dto.ResponseDto
import com.artemkaxboy.telerest.service.telegram.TelegramSendService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

// https://www.baeldung.com/spring-webflux
@RestController
@RequestMapping(value = ["api/$API_V1"])
@Tag(name = "Message controller", description = "Perform messages operation")
class MessageController(
    val telegramSendService: TelegramSendService
) {

    @PostMapping(
        "/message",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(summary = "Post message", responses = [ApiResponse(responseCode = "202")])
    fun postMessage(

        @Parameter(description = "Message data")
        @RequestBody(required = true)
        message: MessageDto,

        request: ServerHttpRequest
    ): Mono<ResponseDto> {

        return ResponseDto
            .wrapResult(request, telegramSendService.sendMessage(message.chatId, message.text))
            .toMono()
    }
}
