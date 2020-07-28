package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.config.properties.TelegramBotProperties
import com.artemkaxboy.telerest.entity.User
import com.artemkaxboy.telerest.tool.ExceptionUtils
import com.artemkaxboy.telerest.tool.Result
import com.artemkaxboy.telerest.tool.getOrElse
import com.artemkaxboy.telerest.tool.orElse
import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.elbekD.bot.types.Update
import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

private const val COMMAND_START = "/start"

@Service
class TelegramService(
    private val properties: TelegramBotProperties,
    private val chartService: ChartService,
    private val userService: UserService, // todo delete after adding proper user management
    private val tickerService: TickerService, // todo delete after adding proper user management
    private val subscriptionService: UserTickerSubscriptionService // todo delete after adding proper user management

) {

    private lateinit var bot: Bot

    var started: Boolean = false

    /**
     * Starts the bot.
     *
     * @return occurred error message, null if no error.
     */
    suspend fun start(): Result<Unit> {
        fillDbWithInitDataIfNeeded()

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
            startBot().onSuccess {
                started = true
                return Result.Success(Unit)
            }

            delay(properties.reconnection.delay.toMillis())
        }

        return Result.failure(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't start telegram bot.").log(logger)
    }

    /**
     * Sends text message to given chat.
     *
     * @param text to send.
     * @param sendTo chat ID to send the message to.
     * @return exception safety result of the operation.
     */
    fun sendMessage(sendTo: Long, text: String): Result<Message> =
        Result.of {
            bot.sendMessage(sendTo, text).get()
        }.orElse {
            Result.failure("Cannot send message: $it")
        }

    /**
     * Sends photo to telegram chat.
     *
     * @param sendTo target chatId. Bot cannot start chat, it can send messages to
     * already started personal chats or groups which it had been added to.
     * @param byteArray photo to send.
     * @param caption text to add to the photo.
     * @return id of sent file to reuse it in following messages.
     */
    fun sendPhoto(sendTo: Long, byteArray: ByteArray, caption: String? = null): Result<String> =
        Result.of {
            bot.sendPhoto(sendTo, byteArray, caption).get()?.photo?.get(0)?.file_id
        }.orElse {
            Result.failure("Cannot send photo: $it")
        }

    /**
     * Sends photo to telegram chat.
     *
     * @param sendTo target chatId. Bot cannot start chat, it can send messages to
     * already started personal chats or groups which it had been added to.
     * @param fileId telegram file_id to send.
     * @param caption text to add to the photo.
     * @return id of sent file to reuse it in following messages.
     */
    @Suppress("unused")
    fun sendPhoto(sendTo: Long, fileId: String, caption: String? = null): Result<String> =
        Result.of {
            bot.sendPhoto(sendTo, fileId, caption).get()?.photo?.get(0)?.file_id
        }.orElse {
            Result.failure("Cannot send photo: $it")
        }

    private fun configureBot() {
        bot = Bot.createPolling(properties.botName, properties.token).apply {
            onCommand(COMMAND_START, ::onStartCommand)
            onMessage(::onMessage)
            onAnyUpdate(::onAnyUpdate)
        }
    }

    private fun startBot(): Result<Unit> {
        return Result.of {
            logger.info { "Telegram bot starting..." }
            bot.start()
            logger.info { "Telegram bot started successfully." }
        }.orElse {
            Result
                .failure(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot start telegram bot: ${it.message}")
                .log(logger)
        }
    }

    // ENTRYPOINT
    @Suppress("UNUSED_PARAMETER")
    private suspend fun onStartCommand(message: Message, opts: String?) {
        bot.sendMessage(message.chat.id, "Hello!")
    }

    private suspend fun sendChart(chatId: Long, ticker: String): Result<String> {

        if (ticker.isEmpty()) {
            return Result.failure("Specify ticker. Example: /chart AAPL")
        }

        val chartMessage = chartService.getChartMessage(ticker)
            .getOrElse { exception ->
                return ExceptionUtils.messageOrDefault(exception, prefix = "Cannot get chart for $ticker: ")
                    .let { Result.failure(it) }
            }

        return chartMessage.getByteArray()
            .getOrElse { exception ->
                return ExceptionUtils.messageOrDefault(exception, prefix = ("Cannot generate byte array to send: "))
                    .let { Result.failure(it) }
            }
            .let {
                sendPhoto(chatId, it, chartMessage.caption)
            }
    }

    // ENTRYPOINT
    private suspend fun onMessage(message: Message) {
        message.text?.let { messageText ->
            if (messageText.startsWith("#")) {

                sendChart(message.chat.id, messageText.drop(1).toUpperCase())
                    .onFailure { sendMessage(message.chat.id, ExceptionUtils.messageOrEmpty(it)) }
                return
            }
        }
        logger.info { message }
    }

    // ENTRYPOINT
    @Suppress("RedundantSuspendModifier") // signature is fixed to use in Bot.onCommand
    private suspend fun onAnyUpdate(update: Update) {
        // bot.sendMessage(update.message?.from?.id, "WOW! I didn't expect this!")
        logger.info { "any: $update" }
    }

    // todo delete after making subscribing mechanism
    private fun fillDbWithInitDataIfNeeded() {
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
