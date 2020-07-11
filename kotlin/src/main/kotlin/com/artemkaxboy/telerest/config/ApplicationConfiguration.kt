package com.artemkaxboy.telerest.config

import com.artemkaxboy.telerest.config.properties.ApplicationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.TimeZone


@Configuration
class ApplicationConfiguration(private val applicationProperties: ApplicationProperties) {

    @Bean
    fun configuration() {
        TimeZone.setDefault(TimeZone.getTimeZone(applicationProperties.timeZone))
    }
}
