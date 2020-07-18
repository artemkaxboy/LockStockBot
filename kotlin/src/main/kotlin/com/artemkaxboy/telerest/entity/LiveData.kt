package com.artemkaxboy.telerest.entity

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
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

    @CreationTimestamp
    @Column(updatable = false)
    val created: LocalDateTime?,

    @UpdateTimestamp
    val updated: LocalDateTime?,

    val price: Double,

    val consensus: Double

) : AbstractEntity() {

    companion object {

        fun random() = LiveData(
            date = LocalDate.now().minusDays(Random.nextLong(100, 365)),
            ticker = Ticker.random(),
            price = Random.nextDouble(),
            consensus = Random.nextDouble(),
            created = LocalDateTime.now().minusSeconds(5),
            updated = LocalDateTime.now().minusSeconds(1)
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LiveData

        if (date != other.date) return false
        if (price != other.price) return false
        if (consensus != other.consensus) return false

        return true
    }

    override fun hashCode(): Int {
        var result = date.hashCode()
        result = 31 * result + price.hashCode()
        result = 31 * result + consensus.hashCode()
        return result
    }

    override fun toString(): String {
        return "LiveData(date=$date, price=$price, consensus=$consensus)"
    }
}

data class LiveDataId(

    var date: LocalDate = LocalDate.now(),

    var ticker: String = ""

) : Serializable
