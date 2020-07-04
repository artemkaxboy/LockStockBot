package com.artemkaxboy.telerest.schedule

import com.artemkaxboy.telerest.model.Ticker
import com.artemkaxboy.telerest.repo.TickerRepo
import com.artemkaxboy.telerest.service.forecast.impl.ForecastServiceImpl1
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.ConversionService
import org.springframework.scheduling.annotation.Scheduled

@Configuration
class ScheduledJob(
    private val forecastServiceImpl1: ForecastServiceImpl1,
    private val tickerRepo: TickerRepo,
    private val conversionService: ConversionService
) {

    @Scheduled(fixedRateString = "#{@forecastSource1Properties.updateInterval.toMillis()}")
    fun update() {

        runBlocking { // todo R2DBC will be perfect here
            val list = forecastServiceImpl1.getBufferedFlow()
                .map { conversionService.convert(it, Ticker::class.java) }
                .toList()
            try {
                list.forEach { logger.info { it } }
                val res = tickerRepo.saveAll(list)
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
