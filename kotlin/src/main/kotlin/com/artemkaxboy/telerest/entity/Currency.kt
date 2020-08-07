package com.artemkaxboy.telerest.entity

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "currency")
data class Currency(

    @Id
    val id: String,

    val sign: String = KNOWN_CURRENCIES.getOrDefault(id, id),

    @OneToMany(mappedBy = "currency")
    val tickers: List<Ticker>? = null
) : AbstractEntity() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Currency

        if (id != other.id) return false
        if (sign != other.sign) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + sign.hashCode()
        return result
    }

    override fun toString(): String {
        return "Currency(id='$id', sign='$sign')"
    }

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

        fun getSignOrDefault(currency: Currency?, default: String) = currency?.sign ?: default
    }
}
