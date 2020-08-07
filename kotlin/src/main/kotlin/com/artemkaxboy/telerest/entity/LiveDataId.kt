package com.artemkaxboy.telerest.entity

import java.io.Serializable
import java.time.LocalDate

data class LiveDataId(

    var tickerId: String = "",

    var date: LocalDate = LocalDate.now()
) : Serializable {

    companion object {

        fun of(entity: LiveData): LiveDataId = LiveDataId(entity.tickerId, entity.date)
    }
}
