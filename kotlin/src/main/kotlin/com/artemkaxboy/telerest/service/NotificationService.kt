package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.entity.UserTickerSubscription
import com.artemkaxboy.telerest.listener.event.PotentialChangedEventObject
import com.artemkaxboy.telerest.tool.Emoji
import com.artemkaxboy.telerest.tool.extensions.round
import com.artemkaxboy.telerest.tool.extensions.roundIfNeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.jfree.chart.ChartUtils
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
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
     * Notifies single user through [TelegramService] and updates [UserTickerSubscription.lastNotificationDate].
     */
    private suspend fun notifyUser(update: PotentialChangedEventObject, chatId: Long) {

        val yesterday = update.yesterdayData
        val live = update.liveData
        val currency = live.ticker.currency.sign

        val msg = "#${live.ticker.ticker} ${live.ticker.name}\n" +
            "\nPrice: $currency ${getDiffPercentString(yesterday.price, live.price)}\n" +
            "\nForecast: $currency ${getDiffPercentString(yesterday.consensus, live.consensus)}\n" +
            "\nPotential: ${getDiffString(yesterday.getPotential(), live.getPotential(), 2)}"

        val chart = chartService.getChart(live)

        withContext(Dispatchers.IO) {
            val byteOutputStream = ByteArrayOutputStream()
            ChartUtils.writeChartAsPNG(byteOutputStream, chart, 600, 300)

            telegramService.sendPhoto(chatId, byteOutputStream.toByteArray(), msg)
                .onSuccess { logger.debug { "Photo sent, file_id: $it" } }
        }

        userTickerSubscriptionService
            .updateLastNotificationDate(chatId, update.liveData.ticker.ticker)
    }

    fun getDiffString(from: Double, to: Double, precision: Int = -1): String {
        val fromRounded = from.roundIfNeeded(precision)
        val toRounded = to.roundIfNeeded(precision)

        val diff = (to - from)
            .takeIf { it != 0.0 }
            ?.round(2)
            ?.let { rounded ->
                rounded
                    .takeIf { it > 0 }
                    ?.let { "\n${Emoji.WHITE_UP} $it%" }
                    ?: "\n${Emoji.RED_DOWN} ${rounded.absoluteValue}%"
            }
            ?: ""

        return "$fromRounded -> $toRounded$diff"
    }

    fun getDiffPercentString(from: Double, to: Double, precision: Int = -1): String {
        val fromRounded = from.roundIfNeeded(precision)
        val toRounded = to.roundIfNeeded(precision)

        val diff = ((to - from) / from * 100).takeIf { it != 0.0 }
            ?.round(2)
            ?.let { rounded ->
                rounded.takeIf { it > 0 }
                    ?.let { "\n${Emoji.WHITE_UP} $it%" }
                    ?: "\n${Emoji.RED_DOWN} ${rounded.absoluteValue}%"
            }
            ?: ""

        return "$fromRounded -> $toRounded$diff"
    }

    companion object {

        private val logger = KotlinLogging.logger { }
    }
}
