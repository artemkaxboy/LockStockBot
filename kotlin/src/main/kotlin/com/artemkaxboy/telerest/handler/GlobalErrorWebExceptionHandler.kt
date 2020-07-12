package com.artemkaxboy.telerest.handler

import com.artemkaxboy.telerest.dto.ResponseDto
import com.artemkaxboy.telerest.tool.errors.ErrorProperties
import org.springframework.boot.autoconfigure.web.ResourceProperties
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

// @doc https://programmer.help/blogs/webflux-rest-api-global-exception-handling-error-handling.html
@Component
@Order(-2) // don't delete! Allows to process ALL exception
class GlobalErrorWebExceptionHandler(
    attributes: ErrorAttributes,
    context: ApplicationContext,
    serverCodecConfigurer: ServerCodecConfigurer,
    resourceProperties: ResourceProperties
) : AbstractErrorWebExceptionHandler(attributes, resourceProperties, context) {

    init {
        super.setMessageWriters(serverCodecConfigurer.writers)
        super.setMessageReaders(serverCodecConfigurer.readers)
    }

    override fun getRoutingFunction(errorAttributes: ErrorAttributes): RouterFunction<ServerResponse> {
        return RouterFunctions.route(
            RequestPredicates.all(),
            HandlerFunction { request: ServerRequest ->
                renderErrorResponse(request)
            }
        )
    }

    private fun renderErrorResponse(request: ServerRequest): Mono<ServerResponse> {
        val errorPropertiesMap = getErrorAttributes(
            request,
            ErrorAttributeOptions.of(
                ErrorAttributeOptions.Include.MESSAGE
            )
        )

        // todo map input data validation to 400 not 500

        val code = ErrorProperties.getStatusCode(errorPropertiesMap)

        return ServerResponse.status(code)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(ResponseDto.wrapError(errorPropertiesMap, getError(request)))
    }
}
