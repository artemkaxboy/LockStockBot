package com.artemkaxboy.telerest.model

import io.swagger.annotations.ApiModelProperty
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "users")
data class User(
    @Id
    @ApiModelProperty(readOnly = true)
    var id: String? = null,

    val name: String? = null
)
