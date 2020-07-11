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

        val DUMMY = Currency("_\$_")
    }
}
