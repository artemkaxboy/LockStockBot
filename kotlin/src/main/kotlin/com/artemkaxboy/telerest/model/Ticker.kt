package com.artemkaxboy.telerest.model

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "tickers")
data class Ticker(

    @Id
    val ticker: String = "",

    val name: String = ""
)
