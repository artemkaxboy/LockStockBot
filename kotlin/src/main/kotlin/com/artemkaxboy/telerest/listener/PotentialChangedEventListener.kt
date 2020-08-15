package com.artemkaxboy.telerest.listener

import com.artemkaxboy.telerest.listener.event.PotentialChangedEvent
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
        GlobalScope.launch {
            notificationService.notify(event.source)
        }
    }
}
