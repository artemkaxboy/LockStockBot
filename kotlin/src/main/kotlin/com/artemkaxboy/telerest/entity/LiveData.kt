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
    var date: LocalDate = LocalDate.now(),

    @Id
    @ManyToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "ticker_fk")
    var ticker: Ticker,

    var price: Double,

    var consensus: Double

) : AbstractEntity()

data class LiveDataId(

    var date: LocalDate = LocalDate.now(),

    var ticker: String = ""

): Serializable
