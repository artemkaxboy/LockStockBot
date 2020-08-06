package com.artemkaxboy.telerest.entity

import com.artemkaxboy.telerest.tool.RandomUtils
import javax.persistence.CascadeType
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

    @ManyToOne
    @JoinColumn(nullable = false)
    val currency: Currency,

    val name: String,

    val logo: String,

    @OneToMany(
        mappedBy = "ticker",
        cascade = [CascadeType.REMOVE],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    val subscriptions: Set<UserTickerSubscription> = emptySet(),

    @OneToMany(
        mappedBy = "ticker",
        cascade = [CascadeType.REMOVE],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    val forecasts: Set<Forecast> = emptySet(),

    @OneToMany(
        mappedBy = "ticker",
        cascade = [CascadeType.REMOVE],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    val liveData: Set<LiveData> = emptySet()

) : ChangeableEntity() {

    companion object {

        private val RANDOM_POOL = setOf("BABA", "WB", "JD", "SQ", "AXP", "LUV", "MS", "MAR", "DE", "EBAY", "JPM")

        fun random(): Ticker {
            val randomTickerId = RANDOM_POOL.random()

            return Ticker(
                id = randomTickerId,
                url = RandomUtils.url(randomTickerId),
                currency = Currency.random(),
                name = RandomUtils.company(randomTickerId),
                logo = RandomUtils.url(randomTickerId, "logo.png")
            )
        }
    }

    @Suppress("DuplicatedCode") // supposed to be similar
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Ticker

        if (id != other.id) return false
        if (url != other.url) return false
        if (currency != other.currency) return false
        if (name != other.name) return false
        if (logo != other.logo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + currency.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + logo.hashCode()
        return result
    }

    override fun toString(): String {
        return "Ticker(id='$id', url='$url', currency=$currency, name='$name', logo='$logo')"
    }
}

// todo check the solution https://stackoverflow.com/questions/45642181/kotlin-jpa-encapsulate-onetomany
