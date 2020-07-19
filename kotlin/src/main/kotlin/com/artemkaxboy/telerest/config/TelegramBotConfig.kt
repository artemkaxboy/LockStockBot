package com.artemkaxboy.telerest.config

import com.artemkaxboy.telerest.config.properties.TelegramBotProperties
import com.artemkaxboy.telerest.service.TelegramService
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(TelegramBotProperties::class)
class TelegramBotConfig(
    val telegramBotProperties: TelegramBotProperties
) {

    @Bean
    fun getTelegramBot(): TelegramService {
        return TelegramService(telegramBotProperties)
    }
}
