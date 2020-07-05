package com.artemkaxboy.telerest.schedule

import com.artemkaxboy.telerest.mapper.LiveDataToSource1TickerDtoMapper
import com.artemkaxboy.telerest.service.LiveDataService
import com.artemkaxboy.telerest.service.forecast.impl.ForecastServiceImpl1
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled

@Configuration
class ScheduledJob(
    private val forecastServiceImpl1: ForecastServiceImpl1,
    private val liveDataService: LiveDataService,
    private val liveDataToSource1TickerDtoMapper: LiveDataToSource1TickerDtoMapper
) {

    @Scheduled(fixedRateString = "#{@forecastSource1Properties.updateInterval.toMillis()}")
    fun update() {

        runBlocking { // todo R2DBC will be perfect here
            val list = forecastServiceImpl1.getFlow()
                .map { liveDataToSource1TickerDtoMapper.toEntity(it) }
                .toList()
            try {
                list.forEach { logger.info { it } }
                val res = liveDataService.saveAll(list)
                res.forEach { logger.info { it } }
            } catch (e: Exception) {
                logger.error(e) { e.message }
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
