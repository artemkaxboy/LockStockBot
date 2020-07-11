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

        val DUMMY = LiveData(
            ticker = Ticker.DUMMY,
            price = 10.0,
            consensus = 11.0
        )
    }
}


data class LiveDataId(

    var date: LocalDate = LocalDate.now(),

    var ticker: String = ""

) : Serializable
