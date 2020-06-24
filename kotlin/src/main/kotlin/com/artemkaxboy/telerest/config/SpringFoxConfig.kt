package com.artemkaxboy.telerest.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2WebFlux

const val CURRENT_API_VERSION = "v1"

@Configuration
@EnableSwagger2WebFlux
class SpringFoxConfig {

    @Bean
    fun api(): Docket {
        val info = ApiInfo(
            "Stock forecasts watcher",
            "Operator interface",
            CURRENT_API_VERSION,
            ApiInfo.DEFAULT.termsOfServiceUrl,
            ApiInfo.DEFAULT.contact,
            ApiInfo.DEFAULT.license,
            ApiInfo.DEFAULT.licenseUrl,
            ApiInfo.DEFAULT.vendorExtensions
        )


        return Docket(DocumentationType.SWAGGER_2)
            .apiInfo(info)
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.any())
            .build()
    }
}
