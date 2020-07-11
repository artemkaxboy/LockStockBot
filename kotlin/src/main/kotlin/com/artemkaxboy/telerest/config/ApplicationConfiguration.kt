package com.artemkaxboy.telerest.config

import com.artemkaxboy.telerest.config.properties.ApplicationProperties
import java.util.TimeZone
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfiguration(private val applicationProperties: ApplicationProperties) {

    @Bean
    fun configuration() {
        TimeZone.setDefault(TimeZone.getTimeZone(applicationProperties.timeZone))
    }
}
