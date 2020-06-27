package com.artemkaxboy.telerest.controller

import com.artemkaxboy.telerest.config.API_V1
import com.artemkaxboy.telerest.dto.ResponseDto
import com.artemkaxboy.telerest.model.User
import com.artemkaxboy.telerest.service.UserService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.ApiResponse
import io.swagger.annotations.ApiResponses
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
import springfox.documentation.annotations.ApiIgnore
import javax.persistence.EntityNotFoundException

@RestController
@RequestMapping(value = ["api/$API_V1"])
@Api(tags = ["User controller"], description = "Perform user control operation")
class UserController(
    private val userService: UserService
) {

    @GetMapping(
        "/users",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiOperation(value = "Get all users")
    @ApiResponses(value = [ApiResponse(code = 200, message = "OK")])
    private fun getUsers(
        @RequestParam(required = false, defaultValue = "0")
        page: Int,

        @RequestParam(required = false, defaultValue = "10")
        pageSize: Int,

        @ApiIgnore
        request: ServerHttpRequest
    ): Mono<ResponseDto> {

        return ResponseDto
            .getResponse(request) {
                userService.findAll(PageRequest.of(page, pageSize)).content
            }
            .toMono()
    }

    @GetMapping(
        "/user/{id}",
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ApiOperation(value = "Get user by id")
    @ApiResponses(value = [ApiResponse(code = 200, message = "OK")])
    private fun getUser(
        @PathVariable
        id: Long,

        @ApiIgnore
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
    @ApiOperation(value = "Add user")
    @ApiResponses(value = [ApiResponse(code = 201, message = "Created")])
    private fun postUser(

        @ApiParam(value = "User details")
        @RequestBody(required = true)
        user: User,

        @ApiIgnore
        request: ServerHttpRequest
    ): Mono<ResponseDto> {

        return ResponseDto.getResponse(request) { userService.save(user) }.toMono()
    }
}
