package com.artemkaxboy.telerest.schedule

import com.artemkaxboy.telerest.dto.Source1TickerDto
import com.artemkaxboy.telerest.listener.event.PotentialChangedEvent
import com.artemkaxboy.telerest.mapper.TickerToSource1TickerDtoMapper
import com.artemkaxboy.telerest.mapper.toEntity
import com.artemkaxboy.telerest.service.forecast.impl.ForecastServiceImpl1
import com.artemkaxboy.telerest.service.storage.LiveDataService
import com.artemkaxboy.telerest.service.storage.TickerService
import com.artemkaxboy.telerest.tool.ExceptionUtils.getMessage
import com.artemkaxboy.telerest.tool.getOrElse
import com.artemkaxboy.telerest.tool.paging.SinglePage
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate
import javax.transaction.Transactional

@Component
class UpdateForecastsJob(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val forecastServiceImpl1: ForecastServiceImpl1,
    private val tickerService: TickerService,
    private val liveDataService: LiveDataService
) {

    @Transactional
    @Scheduled(fixedRateString = "#{@forecastSource1Properties.updateInterval.toMillis()}")
    fun update() {
        val error = "Cannot perform scheduled update"

        /* it needs to save only changed tickers */
        val today = liveDataService
            .findAllByDate(LocalDate.now(), SinglePage.unsorted())
            .getOrElse { return logger.warn { it.getMessage(error) } }
            .associateBy { it.tickerId }

        /* it needs to calc potential diff */
        val yesterday = liveDataService
            .findAllByDate(LocalDate.now().minusDays(1), SinglePage.unsorted())
            .getOrElse { return logger.warn { it.getMessage(error) } }
            .associateBy { it.tickerId }

        runBlocking {

            forecastServiceImpl1.takeIf { it.isEnabled() }
                ?.getTickerFlow()
                ?.onEach { logger.trace { "got: ${it.title}" } }
                ?.onEach(::saveMissingForecasts)
                ?.map { liveDataService.getLatestData(it.title, it.price) }
                ?.onEach { logger.trace { "mapped: ${it.tickerId}" } }
                ?.onEach { newTick ->

                    yesterday[newTick.tickerId]
                        ?.takeIf { it.getRoundedPotential() != newTick.getRoundedPotential() }
                        ?.also {
                            applicationEventPublisher.publishEvent(
                                PotentialChangedEvent(newTick, it)
                            )
                        }
                        ?.also { logger.trace { "published: ${it.tickerId}" } }
                }
                ?.filter { today[it.tickerId] != it }
                ?.catch { logger.warn { it.getMessage(error) } }
                // ?.collect { liveDataService.save(it) } // todo measure and choose save/saveAll
                ?.toList()
                ?.let { liveDataService.saveAll(it) }
                ?: logger.info { "ForecastService Source1 disabled." }
        }
    }

    suspend fun saveMissingForecasts(tickerDto: Source1TickerDto) {
        TickerToSource1TickerDtoMapper.instance.toEntity(tickerDto)
            .also { logger.trace { "Saving ticker ${it.id}..." } }
            .also { tickerService.save(it) }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
