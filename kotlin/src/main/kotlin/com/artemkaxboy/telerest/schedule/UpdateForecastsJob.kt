package com.artemkaxboy.telerest.schedule

import com.artemkaxboy.telerest.listener.event.PotentialChangedEvent
import com.artemkaxboy.telerest.mapper.LiveDataToSource1TickerDtoMapper
import com.artemkaxboy.telerest.service.LiveDataService
import com.artemkaxboy.telerest.service.forecast.impl.ForecastServiceImpl1
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class UpdateForecastsJob(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val forecastServiceImpl1: ForecastServiceImpl1,
    private val liveDataService: LiveDataService,
    private val liveDataToSource1TickerDtoMapper: LiveDataToSource1TickerDtoMapper
) {

    @Scheduled(fixedRateString = "#{@forecastSource1Properties.updateInterval.toMillis()}")
    fun update() {

        /* it needs to save only changed tickers */
        val lastTick = liveDataService
            .findAllLatest()
            .map { it.ticker.ticker to it }
            .toMap()

        /* it needs to calc potential diff */
        val yesterday = liveDataService
            .findAllByDate(LocalDate.now().minusDays(1), Pageable.unpaged()).getContent()
            .map { it.ticker.ticker to it }
            .toMap()

        runBlocking {
            
            forecastServiceImpl1.getFlow()
                .mapNotNull { liveDataToSource1TickerDtoMapper.toEntity(it) }
                .onEach { newTick ->

                    yesterday[newTick.ticker.ticker]
                        ?.takeIf { it.getPotential() != newTick.getPotential() }
                        ?.run {
                            applicationEventPublisher.publishEvent(
                                PotentialChangedEvent(
                                    newTick,
                                    this
                                )
                            )
                        }
                }
                .filter { lastTick[it.ticker.ticker] != it }
                .toList()
                .let { liveDataService.saveAll(it) }
        }
    }
}
