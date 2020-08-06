package com.artemkaxboy.telerest.entity

import com.artemkaxboy.telerest.tool.RandomUtils
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@IdClass(ForecastId::class)
@Table(name = "forecasts")
data class Forecast(

    @Id
    val upstreamId: String = "",

    @Id
    val source: Int = 1,

    @Column(nullable = false)
    val publishDate: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val expirationDate: LocalDateTime = LocalDateTime.now(),

    val targetPrice: Double = 0.0,

    val url: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticker_id", nullable = false, updatable = false, insertable = false)
    val ticker: Ticker?,

    @Column(name = "ticker_id")
    val tickerId: String = ticker?.id ?: ""

) : ChangeableEntity() {

    companion object {

        fun random(): Forecast {

            val upstreamId = RandomUtils.forecastId()
            val ticker = Ticker.random()

            return Forecast(
                upstreamId = upstreamId,
                source = RandomUtils.forecastSource(),
                publishDate = RandomUtils.timeBefore(),
                expirationDate = RandomUtils.timeAfter(),
                targetPrice = RandomUtils.price(),
                url = RandomUtils.url(upstreamId),
                ticker = ticker,
                tickerId = ticker.id
            )
        }
    }
}
