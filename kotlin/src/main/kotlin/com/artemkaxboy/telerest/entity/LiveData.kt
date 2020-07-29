package com.artemkaxboy.telerest.entity

import com.artemkaxboy.telerest.tool.NumberUtils
import com.artemkaxboy.telerest.tool.extensions.round
import java.time.LocalDate
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import kotlin.random.Random

@Entity
@IdClass(LiveDataId::class)
@Table(name = "live_data")
data class LiveData(

    @Id
    val date: LocalDate = LocalDate.now(),

    @Id
    @ManyToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "ticker_fk")
    val ticker: Ticker,

    @Column(precision = 5)
    val price: Double,

    @Column(precision = 5)
    val consensus: Double?

) : ChangeableEntity() {

    /**
     * @return growing potential according to the current price and consensus forecast.
     */
    fun getPotential() = consensus?.let { NumberUtils.getPercent(it - price, price) }

    /**
     * @return difference between this [LiveData] potential and the [other]'s.
     */
    fun getPotentialDifferenceOrNull(other: LiveData): Double? {
        return other.getPotential()?.let { getPotential()?.minus(it) }
    }

    fun equalsTo(liveDataShallow: LiveDataShallow?): Boolean {
        if (liveDataShallow == null) return false

        if (date != liveDataShallow.getDate()) return false
        if (ticker.ticker != liveDataShallow.getTicker()) return false
        if (price != liveDataShallow.getPrice()) return false
        if (consensus != liveDataShallow.getConsensus()) return false

        return true
    }

    companion object {

        fun random() = LiveData(
            date = LocalDate.now().minusDays(Random.nextLong(100, 365)),
            ticker = Ticker.random(),
            price = Random.nextDouble().round(4),
            consensus = Random.nextDouble().round(4)
        )
    }
}
