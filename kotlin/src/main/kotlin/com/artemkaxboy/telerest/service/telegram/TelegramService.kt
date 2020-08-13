package com.artemkaxboy.telerest.service.telegram

import com.artemkaxboy.telerest.config.properties.TelegramBotProperties
import com.artemkaxboy.telerest.tool.ExceptionUtils.getMessage
import com.artemkaxboy.telerest.tool.Exiter
import com.artemkaxboy.telerest.tool.common.Repetition
import com.artemkaxboy.telerest.tool.Result
import com.artemkaxboy.telerest.tool.extensions.toHuman
import com.elbekD.bot.Bot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class TelegramService(
    private val properties: TelegramBotProperties,
    private val exiter: Exiter
) {

    final var bot: Bot? = null
        private set

    fun getBot(): Result<Bot> =
        bot?.let { Result.success(it) }
            ?: Result.failure("Bot is not ready, current state: $state")

    var state: BotState = BotState.OFF

    val stateListeners = mutableListOf<BotStateListener>()

    fun addStateListener(listener: BotStateListener) {
        stateListeners.add(listener)
    }

    private fun onBotCreated(bot: Bot) = onBotStateChanged(bot, BotState.CREATED)
    private fun onBotStarted(bot: Bot) = onBotStateChanged(bot, BotState.STARTED)

    private fun onBotStateChanged(bot: Bot, newState: BotState) {
        state = newState
        stateListeners.forEach { it.onStateChanged(bot, newState) }
    }

    /**
     * Starts the bot if it's enabled and token provided. Shutdown the application on error.
     */
    @EventListener(ApplicationReadyEvent::class)
    fun startIfNeeded() {

        GlobalScope
            .takeIf { isStartEnabled() }
            ?.launch {

                // Start the bot or kill the app
                start().onFailure { exiter.error(it.getMessage()) }
            }
    }

    /**
     * Starts the bot if it's enabled and token provided.
     *
     * @return occurred error message, null if no error.
     */
    suspend fun start(): Result<Unit> {

        val attemptCount = properties.reconnection.count
        val delay = properties.reconnection.delay
        val newBot = createBot()

        bot = Repetition<Bot>(attemptCount, delay)
            .onAttemptFailure { exception, attempt ->
                val msg = "Could not start telegram bot, attempt $attempt / $attemptCount. Reason"
                logger.warn { exception.getMessage(msg) }
            }
            .beforeDelay { logger.info { "Next attempt in ${delay.toHuman()}" } }
            .start { startBot(newBot) }
            ?: return Result.failure("Could not start telegram bot.")

        return Result.success(Unit)
    }

    private fun isStartEnabled(): Boolean {
        if (!properties.enabled) {
            logger.info { "Telegram bot disabled." }
            return false
        }

        if (properties.token.isEmpty()) {
            logger.info { "Telegram token is empty. Telegram bot disabled." }
            return false
        }

        return true
    }

    private fun createBot(): Bot {
        return Bot.createPolling(properties.botName, properties.token)
            .also { onBotCreated(it) }
    }

    private suspend fun startBot(bot: Bot): Result<Bot> {

        return withContext(Dispatchers.IO) {
            Result.of {
                logger.info { "Telegram bot starting..." }
                bot.start()
                onBotStarted(bot)
                logger.info { "Telegram bot started successfully." }
                bot
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
