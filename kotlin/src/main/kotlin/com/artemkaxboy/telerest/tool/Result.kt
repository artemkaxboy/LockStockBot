package com.artemkaxboy.telerest.tool

import com.artemkaxboy.telerest.tool.ExceptionUtils.getMessage
import org.springframework.data.domain.Page

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

    inline fun onFailure(action: (exception: Exception) -> Unit): Result<T> {
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
                block()?.let { success(it) }
                    ?: failure(Exception("Got null result"), errorMessage)
            } catch (e: Exception) {
                failure(e, errorMessage)
            }
        }
    }
}

inline fun <T : Any> Result<T>.getOrElse(onFailure: (exception: Exception) -> T): T {
    return when (val exception = exceptionOrNull()) {
        null -> requireNotNull(getOrNull())
        else -> onFailure(exception)
    }
}

fun <T : Any, I : List<T>, R : Any> Result<I>.map(block: (T) -> R): Result<List<R>> {

    return getOrElse { return Result.failure(it) }
        .map(block)
        .let(Result.Companion::success)
}

fun <T : Any, I : Page<T>, R : Any> Result<I>.mapPage(block: (T) -> R): Result<Page<R>> {

    return getOrElse { return Result.failure(it) }
        .map(block)
        .let(Result.Companion::success)
}
