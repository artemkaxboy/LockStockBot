package com.artemkaxboy.telerest.schedule

import com.artemkaxboy.telerest.service.forecast.impl.ForecastServiceImpl1
import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled

@Configuration
class ScheduledJob(private val forecastServiceImpl1: ForecastServiceImpl1) {

    @Scheduled(fixedRate = 1000000)
    fun update() {
        forecastServiceImpl1.getList()
        logger.info("boom")
    }

    companion object {
        private val logger = KotlinLogging.logger {  }
    }
}
