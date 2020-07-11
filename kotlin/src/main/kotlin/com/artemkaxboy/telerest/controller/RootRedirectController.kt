package com.artemkaxboy.telerest.controller

import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import springfox.documentation.annotations.ApiIgnore
import java.net.URI


@ApiIgnore
@RestController
class RootRedirectController {

    @GetMapping(value = ["/", "/api", "/api/"])
    fun getEcho(response: ServerHttpResponse): Mono<Void> {
        response.statusCode = HttpStatus.MOVED_PERMANENTLY
        response.headers.location = URI.create("/swagger-ui.html")
        return response.setComplete()
    }
}
