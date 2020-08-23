package com.artemkaxboy.telerest.tool.common

import com.artemkaxboy.telerest.tool.Result
import kotlinx.coroutines.delay
import java.time.Duration

/**
 * Class helps to reduce boilerplate code working with repeatable attempts to do something.
 *
 * Standard flow to work with:
 * ```
 * val attemptsCount = 5
 * val delay = 10000L
 *
 * Repetition<Int>(attemptsCount, delay)
 *     .onAttemptFailure { exception, attempt ->
 *         println("Could not perform the task, attempt $attempt / $attemptsCount")
 *     }
 *     .beforeDelay { println("Next attempt in ${delay}ms") }
 *     .start { countSomethingOnline() }
 * ```
 */
class Repetition<T : Any>(private val times: Int, private val delay: Long) {

    constructor(times: Int, delay: Duration) : this(times, delay.toMillis())

    private val failureListeners = mutableListOf<((Exception, Int) -> Unit)>()

    private val beforeDelayListeners = mutableListOf<(Long) -> Unit>()

    /**
     * Starts [Repetition] flow. Perform provided function until it returns [Result.Success]
     * or attempts count is reached.
     */
    suspend fun start(block: suspend () -> Result<T>): T? {

        for (index in 1..times) {

            block()
                .onSuccess { return it }
                .onFailure { exception ->
                    failureListeners.forEach {
                        it(exception, index)
                    }
                }

            if (index != times) {
                beforeDelayListeners.forEach {
                    it(delay)
                }
                delay(delay)
            }
        }

        return null
    }

    /**
     * Function to be call when attempt fails. It will be called each time when attempt fails.
     */
    fun onAttemptFailure(listener: ((Exception, Int) -> Unit)): Repetition<T> {
        failureListeners.add(listener)
        return this
    }

    /**
     * Function to be call before each delay.
     */
    fun beforeDelay(listener: (Long) -> Unit): Repetition<T> {
        beforeDelayListeners.add(listener)
        return this
    }
}
