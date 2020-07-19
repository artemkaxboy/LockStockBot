package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.entity.Ticker
import com.artemkaxboy.telerest.entity.User
import com.artemkaxboy.telerest.entity.UserTickerSubscription
import com.artemkaxboy.telerest.repo.UserTickerSubscriptionRepo
import org.springframework.stereotype.Service
import java.time.LocalDate
import javax.transaction.Transactional

@Service
class UserTickerSubscriptionService(
    private val userTickerSubscriptionRepo: UserTickerSubscriptionRepo
) {

    /**
     * Finds all subscriptions that crossed threshold but are still unnotified today.
     */
    fun findAllUnnotified(ticker: String, difference: Double) =
        userTickerSubscriptionRepo
            .findAllByTicker_TickerAndThresholdLessThanAndLastNotificationDateBefore(
                ticker, difference, LocalDate.now()
            )

    /**
     * Updates subscription notification date to prevent multiple notifications.
     */
    @Transactional
    fun updateLastNotificationDate(user: Long, ticker: String) =
        userTickerSubscriptionRepo
            .updateLastNotificationDateByUser_ChatIdAndTicker_Ticker(LocalDate.now(), user, ticker)

    /**
     * Deletes all subscriptions. Test purposes only.
     */
    @Transactional
    fun deleteAll() = userTickerSubscriptionRepo.deleteAllInBatch()

    /**
     * Subscribe all users to all tickers. Test purposes only.
     */
    @Transactional
    fun allToAll(users: List<User>, tickers: List<Ticker?>) {
        users.flatMap { user ->
            tickers.filterNotNull()
                .map { ticker ->
                    UserTickerSubscription(
                        user = user,
                        ticker = ticker,
                        threshold = 5.0,
                        lastNotificationDate = LocalDate.now().minusDays(1)
                    )
                }
        }.forEach {
            userTickerSubscriptionRepo.save(it)
        }
    }
}
