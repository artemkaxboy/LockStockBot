package com.artemkaxboy.telerest.entity

import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

// @doc https://www.baeldung.com/jpa-many-to-many#many-to-many-with-a-new-entity

/**
 * Entity represents user's subscription to the ticket growing potential changes.
 */
@Entity
@IdClass(UserTickerSubscriptionId::class)
@Table(name = "user_ticker_subscription")
data class UserTickerSubscription(

    @Id
    @ManyToOne
    @JoinColumn(nullable = false)
    val user: User,

    // TODO make userId, tickerId fields

    @Id
    @ManyToOne
    @JoinColumn(nullable = false)
    val ticker: Ticker,

    val lastNotificationDate: LocalDate,

    val threshold: Double

) : AbstractEntity()
