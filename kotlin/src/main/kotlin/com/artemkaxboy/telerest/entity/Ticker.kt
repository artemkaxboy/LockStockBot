package com.artemkaxboy.telerest.entity

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "tickers")
data class Ticker(

    @Id
    val ticker: String,

    val url: String,

    @OneToOne(cascade = [CascadeType.ALL])
    val currency: Currency,

    val name: String,

    val logo: String

) : AbstractEntity() {

    companion object {

        private val RANDOM_POOL = setOf("BABA", "WB", "JD", "SQ", "AXP", "LUV", "MS", "MAR", "DE", "EBAY", "JPM")

        fun random(): Ticker {
            val randomTicker = RANDOM_POOL.random()

            return Ticker(
                ticker = randomTicker,
                url = "http://$randomTicker.url/",
                currency = Currency.random(),
                name = "Company $randomTicker",
                logo = "http://$randomTicker.url/logo.png"
            )
        }
    }
}

// todo check the solution https://stackoverflow.com/questions/45642181/kotlin-jpa-encapsulate-onetomany
