package com.artemkaxboy.telerest.schedule

import com.artemkaxboy.telerest.listener.event.PotentialChangedEvent
import com.artemkaxboy.telerest.listener.event.UpdateFinishedEvent
import com.artemkaxboy.telerest.service.forecast.impl.ForecastSourceServiceImpl1
import com.artemkaxboy.telerest.service.storage.LiveDataService
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
    private val forecastSourceServiceImpl1: ForecastSourceServiceImpl1,
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

            forecastSourceServiceImpl1.takeIf { it.isEnabled() }
                ?.getTickerFlow()
                ?.onEach { logger.trace { "got: ${it.title}" } }
                ?.onEach(forecastSourceServiceImpl1::saveMissingForecasts)
                ?.map { liveDataService.getLatestData(it.title, it.price) }
                ?.onEach { logger.trace { "mapped: ${it.tickerId}" } }
                ?.onEach { newTick ->

                    yesterday[newTick.tickerId]
                        ?.takeIf { it.getRoundedPotential() != newTick.getRoundedPotential() }
                        ?.also {
                            applicationEventPublisher.publishEvent(
                                PotentialChangedEvent(PotentialChangedEvent.Source(newTick, it))
                            )
                        }
                        ?.also { logger.trace { "published: ${it.tickerId}" } }
                }
                ?.filter { today[it.tickerId] != it }
                ?.catch { logger.warn { it.getMessage(error) } }
                // ?.collect { liveDataService.save(it) } // todo measure and choose save/saveAll
                ?.toList()
                ?.let { liveDataService.saveAll(it) }
                ?.also { applicationEventPublisher.publishEvent(UpdateFinishedEvent()) }
                ?: logger.info { "ForecastService Source1 disabled." }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
