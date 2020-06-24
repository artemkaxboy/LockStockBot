package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.config.API_V1
import com.artemkaxboy.telerest.model.User
import com.artemkaxboy.telerest.repo.UserRepository
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import springfox.documentation.annotations.ApiIgnore

// https://www.baeldung.com/spring-webflux
@RestController
@RequestMapping(value = ["api/$API_V1"])
@Api(tags = ["User controller"], description = "Perform user control operation")
class UserController(
    val userRepository: UserRepository
) {

    @GetMapping(
        "/users",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiOperation(value = "Get all users")
    @ApiResponses(value = [ApiResponse(code = 200, message = "OK")])
    private fun getUsers(

        @ApiIgnore
        request: ServerHttpRequest
    ): Flux<User> {

        return userRepository.findAll()
    }

    @PostMapping(
        "/user",
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiOperation(value = "Add user")
    @ApiResponses(value = [ApiResponse(code = 201, message = "Created")])
    private fun postUser(

        @ApiParam(value = "User details")
        @RequestBody(required = true)
        user: User,

        @ApiIgnore
        request: ServerHttpRequest
    ): Mono<User> {

        return userRepository.save(user)
    }
}
