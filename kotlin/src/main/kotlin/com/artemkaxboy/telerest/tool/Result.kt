package com.artemkaxboy.telerest.tool

import com.artemkaxboy.telerest.tool.ExceptionUtils.getMessage
import mu.KLogger

// @doc https://stackoverflow.com/a/59168658/1452052

/**
 * @see [kotlin.Result]
 */
sealed class Result<out T : Any>(
    protected val value: T? = null,
    protected val exception: Exception? = null
) {
    abstract fun isSuccess(): Boolean

    fun getOrNull() = value

    inline fun onSuccess(action: (value: T) -> Unit): Result<T> {
        if (isSuccess()) {
            action(requireNotNull(getOrNull()))
        }
        return this
    }

    fun isFailure() = !isSuccess()
    fun exceptionOrNull() = exception

    inline fun onFailure(action: (exception: Throwable) -> Unit): Result<T> {
        if (isFailure()) {
            action(requireNotNull(exceptionOrNull()))
        }
        return this
    }

    class Success<U : Any> internal constructor(data: U) : Result<U>(data) {

        override fun isSuccess() = true

        override fun toString(): String {
            return "Success[$value]"
        }
    }

    class Failure internal constructor(exception: Exception) :
        Result<Nothing>(exception = exception) {

        override fun isSuccess() = false

        fun log(logger: KLogger): Failure {
            logger.error { exception.toString() }
            return this
        }

        override fun toString(): String {
            return "Failure[$exception]"
        }
    }

    companion object {

        fun failure(message: String) = Failure(Exception(message))

        fun failure(exception: Exception, message: String? = null): Failure {
            val extendedException = message
                ?.let { Exception(exception.getMessage(message), exception) }
                ?: exception
            return Failure(extendedException)
        }

        fun <R : Any> success(value: R) = Success(value)

        inline fun <R : Any> of(errorMessage: String? = null, block: () -> R?): Result<R> {
            return try {
                success(requireNotNull(block()))
            } catch (e: Exception) {
                failure(e, errorMessage)
            }
        }
    }
}

inline fun <T : Any> Result<T>.getOrElse(onFailure: (exception: Throwable) -> T): T {
    return when (val exception = exceptionOrNull()) {
        null -> requireNotNull(getOrNull())
        else -> onFailure(exception)
    }
}
