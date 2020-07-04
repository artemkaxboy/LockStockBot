package com.artemkaxboy.telerest.model

import java.util.Date
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Table

// current solution involves additional update query for each forecast to
// insert ticker id
// current solution writes null in existed forecasts ticker_id field
// https://docs.jboss.org/hibernate/orm/4.1/manual/en-US/html/ch07.html#collections-bidirectional

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

    val price: Double,

    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name="ticker_fk")
    var forecasts: List<Forecast>
)

@Entity
@Table(name = "currency")
data class Currency(

    @Id
    val id: String = ""
)

@Entity
@Table(name = "forecasts")
class Forecast(

    val publishDate: Date,

    val expiationDate: Date,

    val targetPrice: Double,

    @ManyToOne
    @JoinColumn(name="ticker_fk", insertable=false, updatable=false)
    var ticker: Ticker? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id = 0L

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Forecast

        if (publishDate != other.publishDate) return false
        if (expiationDate != other.expiationDate) return false
        if (targetPrice != other.targetPrice) return false
        if (ticker?.name != other.ticker?.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publishDate.hashCode()
        result = 31 * result + expiationDate.hashCode()
        result = 31 * result + targetPrice.hashCode()
        result = 31 * result + (ticker?.name?.hashCode() ?: 0)
        return result
    }
}


// todo check the solution https://stackoverflow.com/questions/45642181/kotlin-jpa-encapsulate-onetomany
