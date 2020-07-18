package com.artemkaxboy.telerest.entity

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
    val consensus: Double

) : ChangeableEntity() {

    fun getPotential() = ((consensus - price) / price * 100)

    companion object {

        fun random() = LiveData(
            date = LocalDate.now().minusDays(Random.nextLong(100, 365)),
            ticker = Ticker.random(),
            price = Random.nextDouble(),
            consensus = Random.nextDouble()
        )
    }
}
