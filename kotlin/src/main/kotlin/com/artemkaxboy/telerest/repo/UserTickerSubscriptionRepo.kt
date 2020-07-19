package com.artemkaxboy.telerest.repo

import com.artemkaxboy.telerest.entity.UserTickerSubscription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

/**
 * Subscriptions repository.
 */
@Suppress("FunctionName") // jpa naming template
@Repository
interface UserTickerSubscriptionRepo : JpaRepository<UserTickerSubscription, Long> {

    /**
     * Works mostly with [LocalDate.now]. Finds all subscriptions that crossed threshold but are still unnotified today.
     */
    fun findAllByTicker_TickerAndThresholdLessThanAndLastNotificationDateBefore(
        ticker: String,
        notificationThreshold: Double,
        lastNotificationDate: LocalDate
    ): List<UserTickerSubscription?>

    /**
     * Updates subscription notification date to prevent multiple notifications.
     */
    @Modifying
    @Query("UPDATE UserTickerSubscription SET lastNotificationDate = :lastNotificationDate WHERE user.chatId = :userChatId AND ticker.ticker = :ticker")
    fun updateLastNotificationDateByUser_ChatIdAndTicker_Ticker(
        lastNotificationDate: LocalDate,
        userChatId: Long,
        ticker: String
    )
}
