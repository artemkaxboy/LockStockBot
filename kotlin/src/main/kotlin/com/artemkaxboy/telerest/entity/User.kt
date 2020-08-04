package com.artemkaxboy.telerest.entity

import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "users")
data class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(unique = true, nullable = false)
    val chatId: Long,

    @Column(nullable = false)
    val name: String,

    val customReadAllowed: Boolean = false,

    val customSubscriptionAllowed: Boolean = false,

    val commonSubscriptionAllowed: Boolean = false,

    @OneToMany(mappedBy = "user", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    val subscriptions: Set<UserTickerSubscription> = emptySet()

) : AbstractEntity() {

    @Suppress("DuplicatedCode") // equals is supposed to be similar
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (chatId != other.chatId) return false
        if (name != other.name) return false
        if (customReadAllowed != other.customReadAllowed) return false
        if (customSubscriptionAllowed != other.customSubscriptionAllowed) return false
        if (commonSubscriptionAllowed != other.commonSubscriptionAllowed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + chatId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + customReadAllowed.hashCode()
        result = 31 * result + customSubscriptionAllowed.hashCode()
        result = 31 * result + commonSubscriptionAllowed.hashCode()
        return result
    }

    override fun toString(): String {
        return "User(id=$id, chatId=$chatId, name='$name', customReadAllowed=$customReadAllowed, " +
            "customSubscriptionAllowed=$customSubscriptionAllowed, " +
            "commonSubscriptionAllowed=$commonSubscriptionAllowed)"
    }
}
