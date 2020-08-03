package com.artemkaxboy.telerest.entity

import java.io.Serializable
import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.FetchType
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
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Id
    @ManyToOne
    @JoinColumn(name = "ticker_id", nullable = false)
    val ticker: Ticker,

    val lastNotificationDate: LocalDate,

    val threshold: Double

) : AbstractEntity()

data class UserTickerSubscriptionId(

    var user: Long = 0,

    var ticker: String = ""

) : Serializable
