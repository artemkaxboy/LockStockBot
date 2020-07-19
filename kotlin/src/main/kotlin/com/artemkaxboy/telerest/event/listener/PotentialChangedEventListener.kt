package com.artemkaxboy.telerest.event.listener

import com.artemkaxboy.telerest.event.PotentialChangedEvent
import com.artemkaxboy.telerest.event.PotentialChangedEventObject
import com.artemkaxboy.telerest.service.NotificationService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class PotentialChangedEventListener(
    val notificationService: NotificationService
) : ApplicationListener<PotentialChangedEvent> {

    /**
     * Listen each [PotentialChangedEvent] and starts coroutine to notify subscribers.
     */
    override fun onApplicationEvent(event: PotentialChangedEvent) {
        val changes = event.source as? PotentialChangedEventObject
            ?: return

        GlobalScope.launch {
            notificationService.notify(changes)
        }
    }
}
