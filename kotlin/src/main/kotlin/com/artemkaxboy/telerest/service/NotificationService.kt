package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.listener.event.PotentialChangedEventObject
import com.artemkaxboy.telerest.tool.ExceptionUtils
import com.artemkaxboy.telerest.tool.Result
import com.artemkaxboy.telerest.tool.getOrElse
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.math.absoluteValue

/**
 * Service to send notifications to users.
 */
@Service
class NotificationService(
    private val telegramService: TelegramService,
    private val userTickerSubscriptionService: UserTickerSubscriptionService,
    private val chartService: ChartService
) {

    /**
     * Finds all users who are subscribed on ticker updates, set threshold less than current change
     * and has not been notified today and notifies them.
     */
    suspend fun notify(eventObject: PotentialChangedEventObject) {

        /* check input */
        val diff = eventObject.liveData.getPotentialDifferenceOrNull(eventObject.yesterdayData)?.absoluteValue
            ?: return

        userTickerSubscriptionService
            .findAllUnnotified(
                eventObject.liveData.ticker.ticker,
                diff
            )
            .onEach { logger.debug { "Notify ${it.user.name} about ${it.ticker.ticker}" } }
            .forEach { user ->
                GlobalScope.launch {
                    notifyUser(eventObject, user.user.chatId).onFailure {
                        logger.error {
                            ExceptionUtils.messageOrDefault(it, "Cannot notify ${user.user.name}: ")
                        }
                    }
                }
            }
    }

    private suspend fun notifyUser(update: PotentialChangedEventObject, chatId: Long): Result<Unit> = Result.of {

        val live = update.liveData
        val tickerId = live.ticker.ticker
        val info = "user: $chatId, ticker: $tickerId"

        val chartMessage = chartService.getChartMessage(todayData = live).getOrElse {
            return Result.failure(ExceptionUtils.messageOrDefault(it, "Cannot get chart ($info): "))
        }

        chartMessage.getByteArray()
            .getOrElse {
                return Result.failure(
                    ExceptionUtils.messageOrDefault(it, "Cannot generate byte array to send ($info): ")
                )
            }
            .let {
                telegramService.sendPhoto(chatId, it, chartMessage.caption)
            }
            .getOrElse {
                return Result.failure(ExceptionUtils.messageOrDefault(it, "Cannot send graph ($info): "))
            }
            .also {
                logger.trace { "Chart sent ($info), file_id: $it" }
            }

        userTickerSubscriptionService
            .updateLastNotificationDate(chatId, tickerId)
    }

    companion object {

        private val logger = KotlinLogging.logger { }
    }
}
