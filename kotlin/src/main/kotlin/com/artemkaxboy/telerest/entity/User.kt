package com.artemkaxboy.telerest.entity

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType.EAGER
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table


@Entity
@Table(name = "users")
data class User(

    @Id
    val chatId: Long = 0,

    val name: String = "",

    // @OneToMany(fetch = FetchType.EAGER,cascade = [CascadeType.ALL], mappedBy = "ticker")
    // @OrderBy("ticker ASC")
    // @OneToMany(mappedBy = "ticker")
    @OneToMany(fetch = EAGER, cascade = [CascadeType.ALL])
    // @ApiModelProperty(readOnly = true)
    var tickers: List<Ticker?>? = null
)
