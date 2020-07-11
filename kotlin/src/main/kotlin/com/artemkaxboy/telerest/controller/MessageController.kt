package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.config.API_V1
import com.artemkaxboy.telerest.dto.MessageDto
import com.artemkaxboy.telerest.dto.ResponseDto
import com.artemkaxboy.telerest.service.TelegramBot
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import springfox.documentation.annotations.ApiIgnore


// https://www.baeldung.com/spring-webflux
@RestController
@RequestMapping(value = ["api/$API_V1"])
@Api(tags = ["Message controller"], description = "Perform messages operation")
class MessageController(
    val telegramBot: TelegramBot
) {

    @PostMapping(
        "/message",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiOperation(value = "Post message", response = ResponseDto::class)
    @ApiResponses(value = [ApiResponse(code = 202, message = "Accepted")])
    fun postMessage(

        @ApiParam(value = "Message data")
        @RequestBody(required = true)
        message: MessageDto,

        @ApiIgnore
        request: ServerHttpRequest
    ): Mono<ResponseDto> {
        // Mono comes to API, it will be fixed: https://github.com/springfox/springfox/issues/2858

        return ResponseDto
            .getResponse(request) {
                telegramBot.sendMessage(message.text, sendTo = message.chatId)
            }
            .toMono()
    }
}
