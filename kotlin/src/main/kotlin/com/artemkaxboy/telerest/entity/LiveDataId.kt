package com.artemkaxboy.telerest.entity

import java.io.Serializable
import java.time.LocalDate

data class LiveDataId(

    var date: LocalDate = LocalDate.now(),

    var ticker: String = ""

) : Serializable
