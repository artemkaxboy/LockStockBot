package com.artemkaxboy.telerest.listener

import com.artemkaxboy.telerest.service.TelegramBot
import com.artemkaxboy.telerest.tool.Exiter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class AppEvents(
    val exiter: Exiter,
    val telegramBot: TelegramBot
) {

    @EventListener(ApplicationReadyEvent::class)
    fun startApp() {

        GlobalScope.launch {
            if (!telegramBot.start()) {
                exiter.error("Cannot start Telegram Bot. Shutting down...")
            }
        }
    }
}
