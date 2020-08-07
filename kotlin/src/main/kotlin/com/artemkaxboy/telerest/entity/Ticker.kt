package com.artemkaxboy.telerest.entity

import com.artemkaxboy.telerest.tool.RandomUtils
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "tickers")
data class Ticker(

    @Id
    val id: String,

    val url: String,

    // MERGE - Saves new currencies
    @ManyToOne(cascade = [CascadeType.MERGE])
    @JoinColumn(nullable = false, updatable = false, insertable = false)
    val currency: Currency?,

    @Column(name = "currency_id")
    val currencyId: String,

    val name: String,

    val logo: String,

    // REMOVE - to remove subscriptions of this ticker
    @OneToMany(
        mappedBy = "ticker",
        cascade = [CascadeType.REMOVE],
        fetch = FetchType.LAZY
    )
    val subscriptions: List<UserTickerSubscription>? = null,

    // REMOVE - to remove subscriptions of this ticker
    // MERGE - to create forecasts when update ticker
    @OneToMany(
        mappedBy = "ticker",
        cascade = [CascadeType.REMOVE, CascadeType.MERGE],
        fetch = FetchType.LAZY
    )
    val forecasts: List<Forecast>? = null,

    // REMOVE - to remove liveData of this ticker
    @OneToMany(
        mappedBy = "ticker",
        cascade = [CascadeType.REMOVE],
        fetch = FetchType.LAZY
    )
    val liveData: List<LiveData>? = null

) : ChangeableEntity() {

    fun getCurrencySign(): String = Currency.getSignOrDefault(currency, currencyId)

    @Suppress("DuplicatedCode") // supposed to be similar
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Ticker

        if (id != other.id) return false
        if (url != other.url) return false
        if (currencyId != other.currencyId) return false
        if (name != other.name) return false
        if (logo != other.logo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + currencyId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + logo.hashCode()
        return result
    }

    override fun toString(): String {
        return "Ticker(id='$id', url='$url', currencyId='$currencyId', name='$name', logo='$logo')"
    }

    companion object {

        private val RANDOM_POOL = setOf("BABA", "WB", "JD", "SQ", "AXP", "LUV", "MS", "MAR", "DE", "EBAY", "JPM")

        fun random(): Ticker {
            val randomTickerId = RANDOM_POOL.random()
            val randomCurrency = Currency.random()

            return Ticker(
                id = randomTickerId,
                url = RandomUtils.url(randomTickerId),
                currency = randomCurrency,
                currencyId = randomCurrency.id,
                name = RandomUtils.company(randomTickerId),
                logo = RandomUtils.url(randomTickerId, "logo.png")
            )
        }
    }
}

// todo check the solution https://stackoverflow.com/questions/45642181/kotlin-jpa-encapsulate-onetomany
