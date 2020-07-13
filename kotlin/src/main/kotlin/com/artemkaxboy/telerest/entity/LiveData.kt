package com.artemkaxboy.telerest.entity

import java.io.Serializable
import java.time.LocalDate
import javax.persistence.CascadeType
import javax.persistence.Entity
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
    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "ticker_fk")
    val ticker: Ticker,

    val price: Double,

    val consensus: Double

) : AbstractEntity() {

    companion object {

        fun random() = LiveData(
            date = LocalDate.now().minusDays(365),
            ticker = Ticker.random(),
            price = Random.nextDouble(),
            consensus = Random.nextDouble()
        )
    }
}

data class LiveDataId(

    var date: LocalDate = LocalDate.now(),

    var ticker: String = ""

) : Serializable
