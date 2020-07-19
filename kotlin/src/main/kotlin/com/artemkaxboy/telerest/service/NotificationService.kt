package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.entity.UserTickerSubscription
import com.artemkaxboy.telerest.event.PotentialChangedEventObject
import com.artemkaxboy.telerest.tool.extensions.toString
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.math.absoluteValue

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

        with(potentialChangedEventObject) changes@{

            userTickerSubscriptionService
                .findAllUnnotified(
                    this.liveData.ticker.ticker,
                    this.liveData.getPotentialDifference(this.yesterdayData).absoluteValue
                )
                .filterNotNull()
                .onEach { logger.debug { "Notify ${it.user.name} about ${it.ticker.ticker}" } }
                .forEach {
                    GlobalScope.launch {
                        notifyUser(this@changes, it.user.chatId)
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
            "Price: ${yesterday.price} -> ${live.price} $currency\n" +
            "Forecast: ${yesterday.consensus} -> ${live.consensus} $currency\n" +
            "Potential diff: ${live.getPotentialDifference(yesterday).toString(2)}%"
        telegramService.sendMessage(msg, chatId)

        userTickerSubscriptionService
            .updateLastNotificationDate(chatId, update.liveData.ticker.ticker)
    }

    companion object {

        private val logger = KotlinLogging.logger {  }
    }
}
