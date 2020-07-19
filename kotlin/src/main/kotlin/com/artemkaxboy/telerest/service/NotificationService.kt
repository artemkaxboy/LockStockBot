package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.entity.UserTickerSubscription
import com.artemkaxboy.telerest.event.PotentialChangedEventObject
import com.artemkaxboy.telerest.repo.UserTickerSubscriptionRepo
import com.artemkaxboy.telerest.tool.extensions.roundIfNeeded
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.math.absoluteValue

private const val WHITE_DOWN = "\uD83D\uDD3D"
private const val WHITE_UP = "\uD83D\uDD3C"
private const val RED_CROSS = "❌"
private const val RED_DOWN = "\uD83D\uDD3B"
private const val RED_UP = "\uD83D\uDD3A"
private const val RED_CIRCLE = "\uD83D\uDD34"
private const val GREEN_CIRCLE = "\uD83D\uDFE2"

/**
 * Service to send notifications to users.
 */
@Service
class NotificationService(
    private val telegramService: TelegramService,
    private val userTickerSubscriptionService: UserTickerSubscriptionService
) {

    /**
     * Finds all users who are subscribed on ticker updates, set threshold less than current change
     * and has not been notified today and notifies them.
     */
    suspend fun notify(potentialChangedEventObject: PotentialChangedEventObject) {

        with(potentialChangedEventObject) {
            userTickerSubscriptionService
                .findAllUnnotified(
                    this.liveData.ticker.ticker,
                    this.liveData.getPotentialDifference(this.yesterdayData).absoluteValue
                )
                .filterNotNull()
                .onEach { logger.debug { "Notify ${it.user.name} about ${it.ticker.ticker}" } }
                .forEach {
                    GlobalScope.launch {
                        notifyUser(this@with, it.user.chatId)
                    }
                }
        }
    }

    /**
     * Notifies single user through [TelegramService] and updates [UserTickerSubscription.lastNotificationDate]
     */
    private suspend fun notifyUser(update: PotentialChangedEventObject, chatId: Long) {

        val yesterday = update.yesterdayData
        val live = update.liveData
        val currency = live.ticker.currency.id

        val msg = "#${live.ticker.ticker} ${live.ticker.name}\n" +
            "\nPrice: $currency ${getDiffPercentString(yesterday.price, live.price)}\n" +
            "\nForecast: $currency ${getDiffPercentString(yesterday.consensus, live.consensus)}\n" +
            "\nPotential: ${getDiffString(yesterday.getPotential(), live.getPotential(), 2)}"
        telegramService.sendMessage(msg, chatId)

        userTickerSubscriptionService
            .updateLastNotificationDate(chatId, update.liveData.ticker.ticker)
    }

    fun getDiffString(from: Double, to: Double, precision: Int = -1): String {
        val fromRounded = from.takeIf { precision < 0 } ?: from.roundIfNeeded(precision)
        val toRounded = to.takeIf { precision < 0 } ?: to.roundIfNeeded(precision)

        val diff = (to - from).takeIf { it != 0.0 }
            ?.roundIfNeeded(2)
            ?.let { rounded ->
                rounded.takeIf { it > 0 }
                    ?.let { "\n$WHITE_UP $it%" }
                    ?: "\n$RED_DOWN ${rounded.absoluteValue}%"
            }
            ?: ""

        return "$fromRounded -> $toRounded$diff"
    }

    fun getDiffPercentString(from: Double, to: Double, precision: Int = -1): String {
        val fromRounded = from.takeIf { precision < 0 } ?: from.roundIfNeeded(precision)
        val toRounded = to.takeIf { precision < 0 } ?: to.roundIfNeeded(precision)

        val diff = ((to - from) / from * 100).takeIf { it != 0.0 }
            ?.roundIfNeeded(2)
            ?.let { rounded ->
                rounded.takeIf { it > 0 }
                    ?.let { "\n$WHITE_UP $it%" }
                    ?: "\n$RED_DOWN ${rounded.absoluteValue}%"
            }
            ?: ""

        return "$fromRounded -> $toRounded$diff"
    }

    companion object {

        private val logger = KotlinLogging.logger { }
    }
}
