package com.artemkaxboy.telerest.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

const val API_V1 = "v1"

const val CURRENT_API_VERSION = API_V1

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI? {
        return OpenAPI()
            .components(Components())
            .info(
                Info()
                    .title("Stock forecasts watcher")
                    .description("Operator interface.")
                    .version(CURRENT_API_VERSION)
            )
    }
}
