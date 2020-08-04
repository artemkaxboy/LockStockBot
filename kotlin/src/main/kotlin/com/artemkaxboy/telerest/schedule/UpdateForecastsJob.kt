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
    private val liveDataService: LiveDataService,
    private val liveDataToSource1TickerDtoMapper: LiveDataToSource1TickerDtoMapper
) {

    @Scheduled(fixedRateString = "#{@forecastSource1Properties.updateInterval.toMillis()}")
    fun update() {

        /* it needs to save only changed tickers */
        val today = liveDataService
            .findAllByDate(LocalDate.now(), Pageable.unpaged()).getContent()
            .associateBy { it.ticker.id }

        /* it needs to calc potential diff */
        val yesterday = liveDataService
            .findAllByDate(LocalDate.now().minusDays(1), Pageable.unpaged()).getContent()
            .associateBy { it.ticker.id }

        runBlocking {

            forecastServiceImpl1.takeIf { it.isEnabled() }
                ?.getFlow()
                ?.mapNotNull { liveDataToSource1TickerDtoMapper.toEntity(it) }
                ?.onEach { newTick ->

                    yesterday[newTick.ticker.id]
                        ?.takeIf { it.getPotential() != newTick.getPotential() }
                        ?.run {
                            applicationEventPublisher.publishEvent(
                                PotentialChangedEvent(newTick, this)
                            )
                        }
                }
                ?.filter { today[it.ticker.id] != it }
                // ?.collect { liveDataService.save(it) } // test purposes
                ?.toList()
                ?.let { liveDataService.saveAll(it) }
                ?: logger.info { "ForecastService Source1 disabled." }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
