package com.artemkaxboy.telerest.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(title = "Message", description = "Basic message to send to telegram chat.")
data class MessageDto(

    // @ApiModelProperty(
    //     value = "Target chat id.",
    //     example = "30811102"
    // )
    val chatId: Long,

    // @ApiModelProperty(
    //     value = "Message text to send.",
    //     required = true,
    //     allowEmptyValue = false,
    //     example = "Hi, there!"
    // )
    val text: String
) : AbstractDto
