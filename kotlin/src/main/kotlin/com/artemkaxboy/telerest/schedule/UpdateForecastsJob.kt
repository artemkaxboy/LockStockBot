package com.artemkaxboy.telerest.schedule

import com.artemkaxboy.telerest.event.PotentialChangedEvent
import com.artemkaxboy.telerest.mapper.LiveDataToSource1TickerDtoMapper
import com.artemkaxboy.telerest.service.LiveDataService
import com.artemkaxboy.telerest.service.forecast.impl.ForecastServiceImpl1
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate
import javax.transaction.Transactional
import kotlin.math.absoluteValue

@Component
class UpdateForecastsJob(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val forecastServiceImpl1: ForecastServiceImpl1,
    private val liveDataService: LiveDataService,
    private val liveDataToSource1TickerDtoMapper: LiveDataToSource1TickerDtoMapper
) {

    @Transactional
    @Scheduled(fixedRateString = "#{@forecastSource1Properties.updateInterval.toMillis()}")
    fun update() {

        val lastTick = liveDataService
            .findAllLatest()
            .filterNotNull()
            .map { it.ticker.ticker to it }
            .toMap()

        val yesterday = liveDataService
            .findAllByDate(LocalDate.now().minusDays(1), Pageable.unpaged()).getContent()
            .filterNotNull()
            .map { it.ticker.ticker to it }
            .toMap()

        runBlocking {
            forecastServiceImpl1.getFlow()
                .mapNotNull { liveDataToSource1TickerDtoMapper.toEntity(it) }
                .onEach { newTick ->
                    yesterday[newTick.ticker.ticker]
                        ?.getPotential()
                        ?.minus(newTick.getPotential())
                        ?.takeIf { it.absoluteValue != 0.0 }
                        ?.run {
                            applicationEventPublisher.publishEvent(PotentialChangedEvent(newTick, this))
                        }
                }
                .filter { lastTick[it.ticker.ticker] != it }
                .onEach { logger.debug { "Update ${it.ticker.ticker}" } }
                .collect {
                    liveDataService.save(it)
                }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
