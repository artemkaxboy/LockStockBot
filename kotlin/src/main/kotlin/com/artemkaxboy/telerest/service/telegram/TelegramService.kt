package com.artemkaxboy.telerest.service.telegram

import com.artemkaxboy.telerest.config.properties.TelegramBotProperties
import com.artemkaxboy.telerest.tool.Exiter
import com.artemkaxboy.telerest.tool.Result
import com.elbekD.bot.Bot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
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
                start().onFailure { exiter.error(it.toString()) }
            }
    }

    /**
     * Starts the bot if it's enabled and token provided.
     *
     * @return occurred error message, null if no error.
     */
    suspend fun start(): Result<Unit> {

        val newBot = createBot()

        repeat(properties.reconnection.count) {

            startBot(newBot).onSuccess {
                bot = it
                return Result.Success(Unit)
            }

            delay(properties.reconnection.delay.toMillis())
        }

        return Result.failure("Couldn't start telegram bot.").log(logger)
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
            Result.of("Cannot start telegram bot") {
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
