package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.config.properties.TelegramBotProperties
import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.elbekD.bot.types.Update
import kotlinx.coroutines.delay
import mu.KotlinLogging

private const val COMMAND_START = "/start"

class TelegramBot(
    private val properties: TelegramBotProperties
) {

    private lateinit var bot: Bot

    var started = false
    private set

    /**
     * Starts the bot.
     *
     * @return occurred error message, null if no error
     */
    suspend fun start(): String? {
        if (!properties.enabled) {
            logger.info { "Telegram bot disabled." }
            return null
        }

        if (properties.token.isEmpty()) {
            logger.warn { "Telegram token is empty. Telegram bot disabled." }
            return null
        }

        configureBot()

        repeat(properties.reconnection.count) {
            if (startBot()) {
                started = true
                return null
            }
            delay(properties.reconnection.delay.toMillis())
        }
        return "Couldn't start telegram bot."
    }

    fun sendMessage(text: String, sendTo: String = properties.masterChatId): Message {
        return bot.sendMessage(sendTo, text).get()
    }

    private fun configureBot() {
        bot = Bot.createPolling(properties.botName, properties.token)
        bot.onCommand(COMMAND_START, this::onStartCommand)
        bot.onMessage(this::onMessage)
        bot.onAnyUpdate(this::onAnyUpdate)
    }

    private fun startBot(): Boolean {
        return runCatching {
            logger.info { "Telegram bot starting..." }
            bot.start()
            logger.info { "Telegram bot started successfully." }
            true
        }.getOrElse {
            logger.error { "Cannot start telegram bot: ${it.message}" }
            false
        }
    }

    @Suppress("RedundantSuspendModifier", "UNUSED_PARAMETER") // signature is fixed to use in Bot.onCommand
    private suspend fun onStartCommand(message: Message, opts: String?) {
        bot.sendMessage(message.chat.id, "Hello!")
    }

    @Suppress("RedundantSuspendModifier") // signature is fixed to use in Bot.onMessage
    private suspend fun onMessage(message: Message) {
        bot.sendMessage(message.chat.id, "WOW! I didn't expect this message!")
        logger.info { message }
    }

    @Suppress("RedundantSuspendModifier") // signature is fixed to use in Bot.onCommand
    private suspend fun onAnyUpdate(update: Update) {
        // bot.sendMessage(update.message?.from?.id, "WOW! I didn't expect this!")
        logger.info { "any: $update" }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
