package com.artemkaxboy.telerest.entity

import java.io.Serializable

data class UserTickerSubscriptionId(

    var user: Long = 0, // todo add userId field

    var ticker: String = "" // todo add tickerId field

) : Serializable {

    companion object {

        fun of(entity: UserTickerSubscription): UserTickerSubscriptionId =
            UserTickerSubscriptionId(entity.user.id, entity.ticker.id)
    }
}
