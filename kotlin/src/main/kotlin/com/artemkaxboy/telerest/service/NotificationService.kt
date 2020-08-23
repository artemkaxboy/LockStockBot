package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.entity.LiveData
import com.artemkaxboy.telerest.entity.User
import com.artemkaxboy.telerest.service.storage.UserTickerSubscriptionService
import com.artemkaxboy.telerest.service.telegram.TelegramSendService
import com.artemkaxboy.telerest.tool.ExceptionUtils.getMessage
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
    private val telegramSendService: TelegramSendService,
    private val userTickerSubscriptionService: UserTickerSubscriptionService,
    private val chartService: ChartService
) {

    /**
     * Finds all users who are subscribed on ticker updates, set threshold less than current change
     * and has not been notified today and notifies them.
     */
    suspend fun notify(eventObject: LiveData) {

        /* check input */
        val diff = eventObject.getPotentialDifferenceOrNull()?.absoluteValue
            ?: return

        GlobalScope.launch {
            userTickerSubscriptionService
                .findAllUnnotified(
                    eventObject.tickerId,
                    diff
                )
                .onEach { logger.info { "Notify ${it.user.name} about ${it.ticker.id}" } }
                .forEach { user ->
                    notifyUser(eventObject, user.user).onFailure {
                        logger.warn { it.getMessage("Cannot notify ${user.user.name}") }
                    }
                }
        }
    }

    private suspend fun notifyUser(update: LiveData, user: User): Result<Unit> = Result.of {

        val tickerId = update.tickerId
        val info = "user: ${user.name}, ticker: $tickerId"

        val chartMessage = chartService.getChartMessage(todayData = update).getOrElse {
            return Result.failure(it.getMessage("Cannot get chart ($info)"))
        }

        chartMessage.generateByteArray()
            .getOrElse {
                return Result.failure(it.getMessage("Cannot generate byte array to send ($info)"))
            }
            .let {
                telegramSendService.sendPhoto(user.chatId, it, chartMessage.caption)
            }
            .getOrElse {
                return Result.failure(it.getMessage("Cannot send graph ($info)"))
            }
            .also {
                logger.trace { "Chart sent ($info), file_id: $it" }
            }

        userTickerSubscriptionService.updateLastNotificationDate(user.id, tickerId)
    }

    companion object {

        private val logger = KotlinLogging.logger { }
    }
}
