package com.artemkaxboy.telerest.exception

import org.springframework.http.HttpStatus

class RequestException(message: String, val code: HttpStatus, cause: Throwable? = null) :
    RuntimeException(message, cause)
