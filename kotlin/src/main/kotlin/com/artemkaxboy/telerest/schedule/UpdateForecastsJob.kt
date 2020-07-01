package com.artemkaxboy.telerest.schedule

import com.artemkaxboy.telerest.mapper.TickerMapper
import com.artemkaxboy.telerest.model.Ticker
import com.artemkaxboy.telerest.repo.TickerRepo
import com.artemkaxboy.telerest.service.forecast.impl.ForecastServiceImpl1
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.ConversionService
import org.springframework.scheduling.annotation.Scheduled

@Configuration
class ScheduledJob(
    private val forecastServiceImpl1: ForecastServiceImpl1,
    private val tickerMapper: TickerMapper,
    private val tickerRepo: TickerRepo,
    private val conversionService: ConversionService
) {

    @Scheduled(fixedRateString = "#{@forecastSource1Properties.updateInterval.toMillis()}")
    fun update() {

        runBlocking { // todo R2DBC will be perfect here
            val list = forecastServiceImpl1.getBufferedFlow()
                // .map { tickerMapper.toEntity(it) }
                .map { conversionService.convert(it, Ticker::class.java) }
                .collect { logger.info { it } }
                // .toList()
            // tickerRepo.saveAll(list)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
