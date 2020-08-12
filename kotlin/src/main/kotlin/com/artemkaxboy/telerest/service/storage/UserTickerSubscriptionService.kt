package com.artemkaxboy.telerest.service.storage

import com.artemkaxboy.telerest.entity.Ticker
import com.artemkaxboy.telerest.entity.User
import com.artemkaxboy.telerest.entity.UserTickerSubscription
import com.artemkaxboy.telerest.repo.UserTickerSubscriptionRepo
import com.artemkaxboy.telerest.tool.sorting.Sorting
import org.springframework.stereotype.Service
import java.time.LocalDate
import javax.transaction.Transactional

@Service
class UserTickerSubscriptionService(
    private val userTickerSubscriptionRepo: UserTickerSubscriptionRepo
) : BaseStorageService(
    listOf(Sorting(UserTickerSubscription::user))
) {

    /**
     * Deletes all subscriptions. Test purposes only.
     */
    @Transactional
    fun deleteAll() = userTickerSubscriptionRepo.deleteAll()

    /**
     * Finds all subscriptions that crossed threshold but are still unnotified today.
     */
    fun findAllUnnotified(ticker: String, difference: Double): List<UserTickerSubscription> =
        userTickerSubscriptionRepo
            .findAllByTicker_IdAndThresholdLessThanAndLastNotificationDateBefore(
                ticker, difference, LocalDate.now()
            )

    /**
     * Updates subscription notification date to prevent multiple notifications.
     */
    @Transactional
    fun updateLastNotificationDate(user: Long, ticker: String) =
        userTickerSubscriptionRepo
            .updateLastNotificationDateByUser_IdAndTicker_Id(LocalDate.now(), user, ticker)

    /**
     * Subscribe all users to all tickers. Test purposes only.
     */
    @Transactional
    fun allToAll(users: Iterable<User>, tickers: Iterable<Ticker>) {

        users.flatMap { user ->
            tickers
                .map { ticker ->
                    UserTickerSubscription(
                        user = user,
                        ticker = ticker,
                        threshold = 7.5,
                        lastNotificationDate = LocalDate.now().minusDays(1)
                    )
                }
        }.also { userTickerSubscriptionRepo.saveAll(it) }
    }
}
