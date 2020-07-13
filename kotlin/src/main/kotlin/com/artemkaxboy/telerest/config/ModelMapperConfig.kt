package com.artemkaxboy.telerest.config

import org.modelmapper.ModelMapper
import org.modelmapper.convention.MatchingStrategies
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ModelMapperConfig {

    @Bean
    fun getModelMapper(): ModelMapper {
        return ModelMapper().apply {
            configuration.matchingStrategy = MatchingStrategies.STRICT
            configuration.isFieldMatchingEnabled = true
            configuration.isSkipNullEnabled = true
            configuration.fieldAccessLevel = org.modelmapper.config.Configuration.AccessLevel.PRIVATE
        }
    }
}
