package com.artemkaxboy.telerest.entity

import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

// @doc https://www.baeldung.com/jpa-many-to-many#many-to-many-with-a-new-entity

/**
 * Entity represents user's subscription to the ticket growing potential changes.
 */
@Entity
data class UserTickerSubscription(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "chat_id")
    val user: User,

    @ManyToOne
    @JoinColumn(name = "ticker")
    val ticker: Ticker,

    val lastNotificationDate: LocalDate,

    val threshold: Double

) : AbstractEntity()
