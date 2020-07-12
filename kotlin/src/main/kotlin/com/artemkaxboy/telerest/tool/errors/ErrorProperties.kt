package com.artemkaxboy.telerest.tool.errors

import org.springframework.http.HttpStatus

private const val UNKNOWN_PATH = "Unknown"
private const val UNKNOWN_ERROR = "Unknown"
private const val UNKNOWN_REQUEST_ID = "Unknown"

class ErrorProperties(
    val path: String,
    val status: Int,
    val error: String,
    val message: String,
    val requestId: String
) {

    companion object {

        fun getStatusCode(properties: Map<String, Any>): HttpStatus =
            properties["status"]?.toString()?.toIntOrNull()
                ?.let { HttpStatus.resolve(it) }
                ?: HttpStatus.INTERNAL_SERVER_ERROR

        fun from(properties: Map<String, Any>): ErrorProperties {
            return ErrorProperties(
                // properties["timestamp"]?.toString()?.let { LocalDate.parse(it).format(DateTimeFormatter.BASIC_ISO_DATE) }
                //     ?: LocalDate.now(), // unknown format 'Sun Jul 12 07:03:12 UTC 2020'

                properties["path"]?.toString()
                    ?: UNKNOWN_PATH,

                properties["status"]?.toString()?.toIntOrNull()
                    ?: HttpStatus.INTERNAL_SERVER_ERROR.value(),

                properties["error"]?.toString()
                    ?: UNKNOWN_ERROR,

                properties["message"]?.toString()
                    ?: HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,

                properties["requestId"]?.toString()
                    ?: UNKNOWN_REQUEST_ID
            )
        }
    }
}
