package com.artemkaxboy.telerest.listener

import com.artemkaxboy.telerest.listener.event.UpdateFinishedEvent
import com.artemkaxboy.telerest.service.forecast.impl.ForecastSourceServiceImpl1
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class UpdateFinishedEventListener(
    val forecastSourceServiceImpl1: ForecastSourceServiceImpl1
) : ApplicationListener<UpdateFinishedEvent> {

    /**
     * Listen each [UpdateFinishedEvent] and starts coroutine to update analysts.
     */
    override fun onApplicationEvent(event: UpdateFinishedEvent) {

        GlobalScope.launch {
            forecastSourceServiceImpl1.updateAnalysts()
        }
    }
}
