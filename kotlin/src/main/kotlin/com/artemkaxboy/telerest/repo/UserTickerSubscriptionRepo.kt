package com.artemkaxboy.telerest.repo

import com.artemkaxboy.telerest.entity.UserTickerSubscription
import com.artemkaxboy.telerest.entity.UserTickerSubscriptionId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import javax.transaction.Transactional

/**
 * Subscriptions repository.
 */
@Suppress("FunctionName") // jpa naming template
@Repository
interface UserTickerSubscriptionRepo : JpaRepository<UserTickerSubscription, UserTickerSubscriptionId> {

    /**
     * Works mostly with [LocalDate.now]. Finds all subscriptions that crossed threshold but are still unnotified today.
     */
    fun findAllByTicker_IdAndThresholdLessThanAndLastNotificationDateBefore(
        ticker: String,
        notificationThreshold: Double,
        lastNotificationDate: LocalDate
    ): List<UserTickerSubscription>

    /**
     * Updates subscription notification date to prevent multiple notifications.
     */
    @Modifying
    @Query("UPDATE UserTickerSubscription SET lastNotificationDate = :lastNotificationDate " +
        "WHERE user.id = :userId AND ticker.id = :tickerId")
    fun updateLastNotificationDateByUser_IdAndTicker_Id(
        lastNotificationDate: LocalDate,
        userId: Long,
        tickerId: String
    )
}
