package com.artemkaxboy.telerest.entity

import java.time.LocalDate
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.OrderBy
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

    val logo: String,

    @OneToMany(mappedBy = "ticker", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    // @OrderBy("name")
    // @OrderColumn(name = "date")
    // @OrderColumn(name = LiveData::date.name)
    @OrderBy // ordering by primary key is assumed
    var liveData: List<LiveData> = emptyList()
) : AbstractEntity() {

    companion object {

        val EQT = Ticker(
            ticker = "EQT",
            url = "https://quote.rbc.ru/ticker/177530",
            currency = Currency("USD"),
            name = "EQT",
            logo = "https://s0.rbk.ru/emitent_pics/images/78/82/a7d1a99fdc0b0e3a6035c7679b1d8db3.png"
        )
    }
}

@Entity
@Table(name = "currency")
data class Currency(

    @Id
    val id: String
)

@Entity
@Table(name = "live_data")
data class LiveData(

    @Id
    val date: LocalDate = LocalDate.now(),

    @ManyToOne
    // @JoinColumn(name="ticker_fk", insertable=false, updatable=false)
    @JoinColumn(name = "ticker_fk")
    val ticker: Ticker,

    val price: Double,

    val consensus: Double
) {

    override fun toString(): String {
        return "LiveData(date=$date, ticker=${ticker.name}, price=$price, consensus=$consensus)"
    }
}

// todo check the solution https://stackoverflow.com/questions/45642181/kotlin-jpa-encapsulate-onetomany
