package com.artemkaxboy.telerest.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotBlank

@Configuration
@ConfigurationProperties("forecast.source1")
@Validated
class ForecastSource1Properties {

    /** Base url to perform requests */
    @NotBlank(message = "Source1 base url cannot be empty.")
    lateinit var baseUrl: String
}