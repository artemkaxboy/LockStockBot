package com.artemkaxboy.telerest.entity

import com.artemkaxboy.telerest.tool.RandomUtils
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.Index
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@IdClass(ForecastId::class)
@Table(
    name = "forecasts",
    indexes = [Index(name = "analyst_key", columnList = "ticker_id,analyst,publishDate", unique = true)]
)
data class Forecast(

    @Id
    val source: Int = 1,

    @Id
    val upstreamId: String = "",

    val analyst: String? = null,

    @Column(nullable = false)
    val publishDate: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val expirationDate: LocalDate = LocalDate.now(),

    val targetPrice: Double = 0.0,

    val url: String = "",

    // MERGE - create tickers when forecasts comes with new one
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.MERGE])
    @JoinColumn(nullable = false, updatable = false, insertable = false)
    val ticker: Ticker?,

    @Column(name = "ticker_id")
    val tickerId: String = ticker?.id ?: ""

) : ChangeableEntity() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Forecast

        if (analyst != other.analyst) return false
        if (publishDate != other.publishDate) return false
        if (upstreamId != other.upstreamId) return false
        if (source != other.source) return false
        if (expirationDate != other.expirationDate) return false
        if (targetPrice != other.targetPrice) return false
        if (url != other.url) return false
        if (tickerId != other.tickerId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = analyst.hashCode()
        result = 31 * result + publishDate.hashCode()
        result = 31 * result + upstreamId.hashCode()
        result = 31 * result + source
        result = 31 * result + expirationDate.hashCode()
        result = 31 * result + targetPrice.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + tickerId.hashCode()
        return result
    }

    override fun toString(): String {
        return "Forecast(analyst='$analyst', " +
            "publishDate=$publishDate, " +
            "upstreamId='$upstreamId', " +
            "source=$source, " +
            "expirationDate=$expirationDate, " +
            "targetPrice=$targetPrice, " +
            "url='$url', " +
            "tickerId='$tickerId'" +
            ")"
    }

    companion object {

        fun random(tickerId: String? = null): Forecast {

            // No need to generate Ticker object when tickerId provided
            val (tickerIdToAdd, tickerToAdd) = if (tickerId == null) {
                Ticker.random().let { it.id to it }
            } else {
                tickerId to null
            }

            val upstreamId = RandomUtils.forecastId()

            return Forecast(
                upstreamId = upstreamId,
                source = RandomUtils.forecastSource(),
                publishDate = RandomUtils.timeBefore(),
                expirationDate = RandomUtils.dateAfter(),
                targetPrice = RandomUtils.price(),
                url = RandomUtils.url(upstreamId),
                ticker = tickerToAdd,
                tickerId = tickerIdToAdd
            )
        }
    }
}
