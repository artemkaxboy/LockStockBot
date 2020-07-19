package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.config.properties.TelegramBotProperties
import com.artemkaxboy.telerest.entity.User
import com.artemkaxboy.telerest.tool.Result
import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.elbekD.bot.types.Update
import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

private const val COMMAND_START = "/start"

private const val BOT_NULL_ERROR = "Bot is not working"

@Service
class TelegramService(
    private val properties: TelegramBotProperties,
    private val userService: UserService, // todo delete after adding proper user management
    private val tickerService: TickerService, // todo delete after adding proper user management
    private val subscriptionService: UserTickerSubscriptionService // todo delete after adding proper user management

) {

    private var bot: Bot? = null

    var started: Boolean = false

    /**
     * Starts the bot.
     *
     * @return occurred error message, null if no error
     */
    suspend fun start(): Result<Unit> {
        initDb()

        if (!properties.enabled) {
            logger.info { "Telegram bot disabled." }
            return Result.Success(Unit)
        }

        if (properties.token.isEmpty()) {
            logger.warn { "Telegram token is empty. Telegram bot disabled." }
            return Result.Success(Unit)
        }

        configureBot()

        repeat(properties.reconnection.count) {
            startBot().data?.let {
                started = true
                return Result.Success(Unit)
            }

            delay(properties.reconnection.delay.toMillis())
        }

        return Result.Err("Couldn't start telegram bot.", HttpStatus.INTERNAL_SERVER_ERROR).log(logger)
    }

    /**
     * Sends text message to given chat.
     *
     * @param text to send.
     * @param sendTo chat ID to send the message to.
     * @return exception safety result of the operation.
     */
    fun sendMessage(text: String, sendTo: Long) =
        Result.of { bot?.sendMessage(sendTo, text) }
            ?: Result.Err("Bot is not working", HttpStatus.BAD_GATEWAY)

    private fun configureBot() {
        bot = Bot.createPolling(properties.botName, properties.token).apply {
            onCommand(COMMAND_START, ::onStartCommand)
            onMessage(::onMessage)
            onAnyUpdate(::onAnyUpdate)
        }
    }

    private fun startBot(): Result<Unit> {

        return try {
            logger.info { "Telegram bot starting..." }
            bot?.start() ?: return Result.Err(BOT_NULL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR).log(logger)
            logger.info { "Telegram bot started successfully." }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Err("Cannot start telegram bot: ${e.message}", HttpStatus.INTERNAL_SERVER_ERROR)
                .log(logger)
        }
    }

    @Suppress("RedundantSuspendModifier", "UNUSED_PARAMETER") // signature is fixed to use in Bot.onCommand
    private suspend fun onStartCommand(message: Message, opts: String?) {
        bot?.sendMessage(message.chat.id, "Hello!")
    }

    @Suppress("RedundantSuspendModifier") // signature is fixed to use in Bot.onMessage
    private suspend fun onMessage(message: Message) {
        logger.info { message }
    }

    @Suppress("RedundantSuspendModifier") // signature is fixed to use in Bot.onCommand
    private suspend fun onAnyUpdate(update: Update) {
        // bot.sendMessage(update.message?.from?.id, "WOW! I didn't expect this!")
        logger.info { "any: $update" }
    }

    /*
     * First initialization with only one main chat (temporary function).
     */
    private fun initDb() {
        if (properties.mainChatId.isEmpty()) return

        if (userService.count() > 0) return

        subscriptionService.deleteAll()
        userService.deleteAll()

        val users = properties.mainChatId.zip(properties.mainChatName)
            .map { User(it.first, it.second) }
            .toList()
            .also { userService.saveAll(it) }

        val tickers = tickerService.findAll(Pageable.unpaged()).getContent()
        subscriptionService.allToAll(users, tickers)
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
