package com.artemkaxboy.telerest.entity

import com.artemkaxboy.telerest.business.PriceConsensusPotential
import com.artemkaxboy.telerest.tool.Constants
import com.artemkaxboy.telerest.tool.RandomUtils
import com.artemkaxboy.telerest.tool.extensions.round
import org.hibernate.annotations.Formula
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
    val previousPrice: Double? = null,

    @Column(precision = 5)
    val consensus: Double? = null,

    @Column(precision = 5)
    val previousConsensus: Double? = null,

    @Formula("(consensus - price) / price * 100")
    val potential: Double? = null,

    @Formula("(previous_consensus - previous_price) / previous_price * 100")
    val previousPotential: Double? = null

) : ChangeableEntity() {

    private fun getTodayValues() = PriceConsensusPotential(price, consensus, potential)

    private fun getPreviousValues() = PriceConsensusPotential(previousPrice, previousConsensus, previousPotential)

    private fun getValues(previous: Boolean) =
        if (previous) getPreviousValues() else getTodayValues()

    /**
     * @return [potential] value rounded with [Constants.PERCENT_ROUND_PRECISION]
     */
    fun getRoundedPotential(previous: Boolean = false): Double? =
        getValues(previous).potential?.round(Constants.PERCENT_ROUND_PRECISION)

    /**
     * @return difference between this previous data and latest.
     */
    fun getPotentialDifferenceOrNull(): Double? {
        return previousPotential?.let { potential?.minus(it) }
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

        fun random(
            date: LocalDate = RandomUtils.dateBefore(),
            ticker: Ticker? = Ticker.random(),
            tickerId: String = ticker?.id ?: Ticker.random().id,
            price: Double = RandomUtils.price(),
            consensus: Double? = RandomUtils.price()
        ): LiveData {

            require(ticker == null || ticker.id == tickerId)

            return LiveData(date = date, tickerId = tickerId, ticker = ticker, price = price, consensus = consensus)
        }
    }
}
