package com.artemkaxboy.telerest.schedule

import com.artemkaxboy.telerest.service.forecast.impl.ForecastServiceImpl1
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled

@Configuration
class ScheduledJob(
    private val forecastServiceImpl1: ForecastServiceImpl1
) {

    @Scheduled(fixedRateString = "#{@forecastSource1Properties.updateInterval.toMillis()}")
    fun update() {
        runBlocking {
            forecastServiceImpl1.getBufferedFlow().collect {
                logger.info { it.company.title }
            }
        }

        logger.info("boom ${System.currentTimeMillis()}")
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
