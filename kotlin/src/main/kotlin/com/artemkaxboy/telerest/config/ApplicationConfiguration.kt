package com.artemkaxboy.telerest.config

import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

@Configuration
class ApplicationConfiguration {

    @Bean
    fun messageSource(): MessageSource {
        return ReloadableResourceBundleMessageSource().apply {
            setBasename("classpath:messages")
            setDefaultEncoding(Charsets.UTF_8.name())
        }
    }

    @Bean
    fun getValidator(): LocalValidatorFactoryBean? {
        return LocalValidatorFactoryBean().apply {
            setValidationMessageSource(messageSource())
        }
    }
}
