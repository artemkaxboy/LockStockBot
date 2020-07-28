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

    fun getSign() = KNOWN_CURRENCIES.getOrDefault(id, id)

    companion object {

        private val KNOWN_CURRENCIES = mapOf(
            "RUB" to "₽",
            "USD" to "$",
            "EUR" to "€",
            "GBP" to "£",
            "CAD" to "$",
            "AUD" to "$",
            "BYN" to "Br"
        )

        fun random() = KNOWN_CURRENCIES.keys.random().let { Currency(id = it) }
    }
}
