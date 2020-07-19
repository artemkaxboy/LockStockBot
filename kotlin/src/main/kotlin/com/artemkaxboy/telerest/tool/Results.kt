package com.artemkaxboy.telerest.tool

import io.netty.handler.codec.http2.Http2Exception
import mu.KLogger
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

// @doc https://stackoverflow.com/a/59168658/1452052

const val UNKNOWN_ERROR = "Unknown error"
const val NO_ERROR = "No error"

sealed class Result<out T : Any>(
    val data: T? = null,
    val message: String,
    val status: HttpStatus
) {
    abstract fun isError(): Boolean

    class Success<T : Any>(data: T) : Result<T>(data, NO_ERROR, HttpStatus.OK) {
        override fun isError() = false

        override fun toString(): String {
            return "$data"
        }
    }

    class Err(message: String, status: HttpStatus) : Result<Nothing>(message = message, status = status) {
        override fun isError() = true

        fun log(logger: KLogger): Err {
            logger.error { this.toString() }
            return this
        }

        override fun toString(): String {
            return "$status $message"
        }
    }



    companion object {

        fun <R : Any> of(block: () -> R?): Result<R>? {
            return try {
                block()?.let { Success(it) }
            } catch (e: ResponseStatusException) {
                Err(e.message, e.status)
            } catch (e: Exception) {
                Err(e.message ?: UNKNOWN_ERROR, HttpStatus.INTERNAL_SERVER_ERROR)
            }
        }
    }
}
