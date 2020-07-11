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
    var ticker: String,

    var url: String,

    @OneToOne(cascade = [CascadeType.ALL])
    var currency: Currency,

    var name: String,

    var logo: String

) : AbstractEntity() {

    companion object {
        val DUMMY = Ticker(
            "TICK",
            "http://ticker.url/",
            Currency.DUMMY,
            "ticker name",
            "http://ticker.url/logo.png"
        )
    }
}

// todo check the solution https://stackoverflow.com/questions/45642181/kotlin-jpa-encapsulate-onetomany
