package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.config.API_V1
import com.artemkaxboy.telerest.dto.ResponseDto
import com.artemkaxboy.telerest.entity.User
import com.artemkaxboy.telerest.service.storage.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import javax.persistence.EntityNotFoundException
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@RestController
@RequestMapping(value = ["api/$API_V1"])
@Tag(name = "User controller", description = "Perform user control operation")
class UserController(
    private val userService: UserService
) {

    @GetMapping(
        "/users",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(summary = "Get all users")
    fun getUsers(

        @RequestParam(required = false, defaultValue = "1")
        page: Int,

        @RequestParam(required = false, defaultValue = "10")
        pageSize: Int,

        request: ServerHttpRequest
    ): Mono<ResponseDto> {

        return ResponseDto
            .getResponse(request) {
                userService.findAll(PageRequest.of(page - 1, pageSize)).content
            }
            .toMono()
    }

    @GetMapping(
        "/user/{id}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(summary = "Get user by id")
    fun getUser(
        @PathVariable
        id: Long,

        request: ServerHttpRequest
    ): Mono<ResponseDto> {

        return ResponseDto
            .getResponse(request) {
                userService.findById(id)
                    ?: throw EntityNotFoundException("User with id $id not found.")
            }.toMono()
    }

    @PostMapping(
        "/user",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @Operation(summary = "Add user", responses = [ApiResponse(responseCode = "201")])
    fun postUser(

        @RequestBody(required = true)
        user: User,

        request: ServerHttpRequest
    ): Mono<ResponseDto> {

        return ResponseDto.getResponse(request) { userService.save(user) }.toMono()
    }
}
