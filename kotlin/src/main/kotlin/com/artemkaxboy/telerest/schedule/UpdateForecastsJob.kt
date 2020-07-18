package com.artemkaxboy.telerest.schedule

import com.artemkaxboy.telerest.mapper.LiveDataToSource1TickerDtoMapper
import com.artemkaxboy.telerest.service.LiveDataService
import com.artemkaxboy.telerest.service.forecast.impl.ForecastServiceImpl1
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class UpdateForecastsJob(
    private val forecastServiceImpl1: ForecastServiceImpl1,
    private val liveDataService: LiveDataService,
    private val liveDataToSource1TickerDtoMapper: LiveDataToSource1TickerDtoMapper
) {

    @Transactional
    @Scheduled(fixedRateString = "#{@forecastSource1Properties.updateInterval.toMillis()}")
    fun update() {

        runBlocking {
            forecastServiceImpl1.getFlow()
                .mapNotNull { liveDataToSource1TickerDtoMapper.toEntity(it) }
                .onEach { logger.info { it } }
                .collect {
                    liveDataService.save(it)
                }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
