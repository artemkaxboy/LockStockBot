package com.artemkaxboy.telerest.entity

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "currency")
data class Currency(

    @Id
    val id: String

) : AbstractEntity() {

    companion object {

        private val RANDOM_POOL = setOf("RUB", "USD", "EUR", "GBP", "CAD", "AUD", "BYN")

        fun random() = Currency(id = RANDOM_POOL.random())
    }
}
