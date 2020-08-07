package com.artemkaxboy.telerest.entity

import com.artemkaxboy.telerest.tool.Constants
import com.artemkaxboy.telerest.tool.NumberUtils
import com.artemkaxboy.telerest.tool.RandomUtils
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

@Entity
@IdClass(LiveDataId::class)
@Table(name = "live_data")
data class LiveData(

    @Id
    val date: LocalDate = LocalDate.now(),

    // MERGE - to save new tickers if needed
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.MERGE])
    @JoinColumn(nullable = false, updatable = false, insertable = false)
    val ticker: Ticker? = null,

    @Id
    @Column(name = "ticker_id")
    val tickerId: String = ticker?.id ?: "",

    @Column(precision = 5, nullable = false)
    val price: Double = Double.NaN,

    @Column(precision = 5)
    val consensus: Double? = null

) : ChangeableEntity() {

    /**
     * @return growing potential according to the current price and consensus forecast.
     */
    fun getPotential() =
        consensus?.let { NumberUtils.getPercent(it - price, price).round(Constants.ROUND_PRECISION) }

    /**
     * @return difference between this [LiveData] potential and the [other]'s.
     */
    fun getPotentialDifferenceOrNull(other: LiveData): Double? {
        return other.getPotential()?.let { getPotential()?.minus(it) }
    }

    fun equalTo(liveDataShallow: LiveDataShallow?): Boolean {
        if (liveDataShallow == null) return false

        if (date != liveDataShallow.getDate()) return false
        if (tickerId != liveDataShallow.getTickerId()) return false
        if (price != liveDataShallow.getPrice()) return false
        if (consensus != liveDataShallow.getConsensus()) return false

        return true
    }

    @Suppress("DuplicatedCode") // supposed to be similar
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LiveData

        if (date != other.date) return false
        if (tickerId != other.tickerId) return false
        if (price != other.price) return false
        if (consensus != other.consensus) return false

        return true
    }

    override fun hashCode(): Int {
        var result = date.hashCode()
        result = 31 * result + tickerId.hashCode()
        result = 31 * result + price.hashCode()
        result = 31 * result + (consensus?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "LiveData(date=$date, tickerId=$tickerId, price=$price, consensus=$consensus)"
    }

    companion object {

        fun random(): LiveData {

            val ticker = Ticker.random()

            return LiveData(
                date = RandomUtils.dateBefore(),
                tickerId = ticker.id,
                ticker = ticker,
                price = RandomUtils.price(),
                consensus = RandomUtils.price()
            )
        }
    }
}
