package com.artemkaxboy.telerest.schedule

import com.artemkaxboy.telerest.dto.Source1TickerDto
import com.artemkaxboy.telerest.listener.event.PotentialChangedEvent
import com.artemkaxboy.telerest.mapper.ForecastToSource1ForecastDtoMapper
import com.artemkaxboy.telerest.mapper.TickerToSource1TickerDtoMapper
import com.artemkaxboy.telerest.mapper.toEntity
import com.artemkaxboy.telerest.service.storage.TickerService
import com.artemkaxboy.telerest.service.forecast.impl.ForecastServiceImpl1
import com.artemkaxboy.telerest.service.storage.ForecastService
import com.artemkaxboy.telerest.service.storage.LiveDataService
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class UpdateForecastsJob(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val forecastServiceImpl1: ForecastServiceImpl1,
    private val tickerService: TickerService,
    private val forecastService: ForecastService,
    private val liveDataService: LiveDataService
) {

    @Scheduled(fixedRateString = "#{@forecastSource1Properties.updateInterval.toMillis()}")
    fun update() {

        /* it needs to save only changed tickers */
        val today = liveDataService
            .findAllByDate(LocalDate.now(), Pageable.unpaged()).getContent()
            .associateBy { it.tickerId }

        /* it needs to calc potential diff */
        val yesterday = liveDataService
            .findAllByDate(LocalDate.now().minusDays(1), Pageable.unpaged()).getContent()
            .associateBy { it.tickerId }

        runBlocking {

            forecastServiceImpl1.takeIf { it.isEnabled() }
                ?.getFlow()
                ?.onEach(::saveMissingForecasts)
                ?.map { liveDataService.updateAndGet(it.title, it.price) }
                ?.onEach { newTick ->

                    yesterday[newTick.tickerId]
                        ?.takeIf { it.getPotential() != newTick.getPotential() }
                        ?.run {
                            applicationEventPublisher.publishEvent(
                                PotentialChangedEvent(newTick, this)
                            )
                        }
                }
                ?.filter { today[it.tickerId] != it }
                // ?.collect { liveDataService.save(it) } // test purposes
                ?.toList()
                ?.let { liveDataService.saveAll(it) }
                ?: logger.info { "ForecastService Source1 disabled." }
        }
    }

    // TODO save changed and missing only not all
    suspend fun saveMissingForecasts(tickerDto: Source1TickerDto) {
        val tickerId = TickerToSource1TickerDtoMapper.instance.toEntity(tickerDto)
            .also { tickerService.saveIfNotExist(it) }
            .id

        tickerDto.forecasts
            .map { ForecastToSource1ForecastDtoMapper.instance.toEntity(it) }
            .map { it.copy(tickerId = tickerId) }
            .let { forecastService.saveAll(it) }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
