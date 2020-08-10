package com.artemkaxboy.telerest.controller.advice

import com.artemkaxboy.telerest.dto.ResponseDto
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.server.ResponseStatusException
import javax.validation.ConstraintViolationException

@ControllerAdvice
class CustomControllerAdvice {

    @ResponseBody
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(
        exception: ConstraintViolationException,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): ResponseDto {
        return ResponseDto.wrapError(
            request,
            response,
            ResponseStatusException(HttpStatus.BAD_REQUEST, exception.localizedMessage, exception)
        )
    }

    @ResponseBody
    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(
        exception: ResponseStatusException,
        request: ServerHttpRequest,
        response: ServerHttpResponse
    ): ResponseDto {

        return ResponseDto.wrapError(
            request,
            response,
            exception
        )
    }
}
