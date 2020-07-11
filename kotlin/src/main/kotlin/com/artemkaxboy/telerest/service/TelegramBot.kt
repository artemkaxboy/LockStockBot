package com.artemkaxboy.telerest.service

import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.elbekD.bot.types.Update
import kotlinx.coroutines.delay
import mu.KotlinLogging
import java.time.Duration


private
const val COMMAND_START = "/start"


class TelegramBot(
    private val enabled: Boolean,
    private val token: String,
    private val botName: String,
    private val reconnectionCount: Int,
    private val reconnectionDelay: Duration
) {

    private
    lateinit var bot: Bot

    /**
     * Starts the bot.
     *
     * @return true if bot started correct or connection disabled, false - bot couldn't start.
     */
    suspend fun start(): Boolean {
        if (!enabled) {
            logger.info { "Telegram bot disabled." }
            return true
        }

        if (token.isEmpty()) {
            logger.warn { "Telegram token is empty. Telegram bot disabled." }
            return true
        }

        configureBot()

        repeat(reconnectionCount) {
            if (startBot()) {
                return true
            }
            delay(reconnectionDelay.toMillis())
        }
        return false
    }

    fun sendMessage(text: String, sendTo: String): Message {
        return bot.sendMessage(sendTo, text).get()
    }

    private
    fun configureBot() {
        bot = Bot.createPolling(botName, token)
        bot.onCommand(COMMAND_START, this::onStartCommand)
        bot.onMessage(this::onMessage)
        bot.onAnyUpdate(this::onAnyUpdate)
    }


    private
    fun startBot(): Boolean {
        return runCatching {
            logger.info { "Telegram bot starting..." }
            bot.start()
            logger.info { "Telegram bot started successfully." }
            true
        }.getOrElse {
            logger.error { "Cannot start Telegram bot: ${it.message}" }
            false
        }
    }

    @Suppress("RedundantSuspendModifier", "UNUSED_PARAMETER") // signature is fixed to use in Bot.onCommand
    private
    suspend fun onStartCommand(message: Message, opts: String?) {
        bot.sendMessage(message.chat.id, "Hello!")
    }

    @Suppress("RedundantSuspendModifier") // signature is fixed to use in Bot.onMessage
    private
    suspend fun onMessage(message: Message) {
        bot.sendMessage(message.chat.id, "WOW! I didn't expect this message!")
        logger.info { message }
    }

    @Suppress("RedundantSuspendModifier") // signature is fixed to use in Bot.onCommand
    private
    suspend fun onAnyUpdate(update: Update) {
        // bot.sendMessage(update.message?.from?.id, "WOW! I didn't expect this!")
        logger.info { "any: $update" }
    }

    companion object {
        private
        val logger = KotlinLogging.logger { }
    }
}
