package com.artemkaxboy.telerest.entity

import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "users")
data class User(

    @Id
    val chatId: Long,

    val name: String,

    @OneToMany(mappedBy = "user")
    val subscriptions: Set<UserTickerSubscription> = emptySet()

) : AbstractEntity() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (chatId != other.chatId) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = chatId.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    override fun toString(): String {
        return "User(chatId=$chatId, name='$name')"
    }
}
