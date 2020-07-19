package com.artemkaxboy.telerest.event.listener

import com.artemkaxboy.telerest.event.PotentialChangedEvent
import com.artemkaxboy.telerest.event.PotentialChangedEventObject
import com.artemkaxboy.telerest.service.TelegramService
import com.artemkaxboy.telerest.tool.extensions.toString
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class PotentialChangedEventListener(
    val telegramService: TelegramService
) : ApplicationListener<PotentialChangedEvent> {

    override fun onApplicationEvent(event: PotentialChangedEvent) {
        val changes = event.source as? PotentialChangedEventObject
            ?: return

        val msg = "\$${changes.liveData.ticker.ticker}: ${changes.liveData.ticker.name} forecast potential changed, " +
            "value: ${changes.difference.toString(2)}%"
        telegramService.sendMessage(msg)
    }
}
