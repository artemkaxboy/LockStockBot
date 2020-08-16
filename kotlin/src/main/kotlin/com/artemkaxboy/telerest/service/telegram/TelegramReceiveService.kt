package com.artemkaxboy.telerest.service.telegram

import com.artemkaxboy.telerest.config.properties.TelegramBotProperties
import com.artemkaxboy.telerest.service.storage.LiveDataService
import com.artemkaxboy.telerest.service.storage.UserService
import com.artemkaxboy.telerest.tool.ExceptionUtils.getMessage
import com.artemkaxboy.telerest.tool.ExceptionUtils.prefixReason
import com.artemkaxboy.telerest.tool.Result
import com.artemkaxboy.telerest.tool.StringUtils.div
import com.artemkaxboy.telerest.tool.getOrElse
import com.artemkaxboy.telerest.tool.mapPage
import com.artemkaxboy.telerest.tool.telegram.MarkupUtils
import com.elbekD.bot.Bot
import com.elbekD.bot.types.CallbackQuery
import com.elbekD.bot.types.Message
import com.elbekD.bot.types.MessageEntity
import mu.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class TelegramReceiveService(
    telegramBotProperties: TelegramBotProperties,
    private val telegramService: TelegramService,
    private val telegramSendService: TelegramSendService,
    private val userService: UserService,
    private val liveDataService: LiveDataService
) : BotStateListener {

    private val listCommand = "/list"
    private val listSize = telegramBotProperties.markup.listSize

    @PostConstruct
    fun setupService() {
        telegramService.addStateListener(this)
    }

    override fun onStateChanged(bot: Bot, state: BotState) {
        if (state == BotState.CREATED) {
            bot.apply {
                onMessage(::onAnyMessage)
                onCommand(listCommand, ::onListCommand)
                onCallbackQuery(::onAnyCallback)
            }
        }
    }

    // ENTRYPOINT
    private suspend fun onAnyMessage(message: Message) {
        logger.trace { "Got new message (${message.from?.id} ${message.from?.username})" }

        if (isCustomReadRequest(message)) {
            onCustomRead(message)
            return
        }

        logger.info { "Unknown message: $message" }
    }

    // ENTRYPOINT TODO check isCustomReadAllowed
    private suspend fun onListCommand(message: Message, args: String?) {
        logger.trace { "Got list command, args: $args" }
        val error = "Cannot perform list command"

        val page = liveDataService
            .findAllByDate(pageable = PageRequest.of(0, listSize))
            .mapPage(MarkupUtils::mapLiveDataToMarkupPair)
            .getOrElse { return logger.warn { it.getMessage(error) } }

        telegramSendService.sendListMenu(message.chat.id, page, listCommand)
    }

    // ENTRYPOINT
    private suspend fun onAnyCallback(callbackQuery: CallbackQuery) {
        logger.trace { "Got new callback" }
        val error = "Cannot process callback"

        // send ack
        telegramSendService.ackCallback(callbackQuery)
            .onFailure { return logger.warn { it.getMessage(error) } }

        // check values
        val message = callbackQuery.message
            ?: return logger.warn { "No message to reply".prefixReason(error) }

        processCallback(callbackQuery.data, message)
            .onFailure { return logger.warn { it.getMessage(error) } }
    }

    private suspend fun processCallback(
        callbackData: String?,
        message: Message
    ): Result<Unit> {
        val (prefix, command) = callbackData.orEmpty() / ":"

        return when (prefix) {
            listCommand ->
                updateList(command, message, prefix)

            else ->
                Result.failure("Unknown prefix $prefix")
        }
    }

    private suspend fun updateList(
        command: String?,
        message: Message,
        prefix: String
    ): Result<Unit> {
        val error = "Cannot update list"

        val (arg, value) = command.orEmpty() / "="

        when (arg) {

            "page" -> {
                val pageNumber = value?.toIntOrNull() ?: 0

                val page = liveDataService.findAllByDate(pageable = PageRequest.of(pageNumber, listSize))
                    .mapPage(MarkupUtils::mapLiveDataToMarkupPair)
                    .getOrElse { return Result.failure(it.getMessage(error)) }

                telegramSendService.updateListMenu(message, page, prefix)
                    .onFailure { return Result.failure(it.getMessage(error)) }
            }

            "" -> { }

            else -> {
                telegramSendService.clearMenu(message)
                    .onFailure { return Result.failure(it.getMessage(error)) }

                telegramSendService.sendChart(message.chat.id, arg)
                    .onFailure { return Result.failure(it.getMessage(error)) }
            }
        }

        return Result.success(Unit)
    }

    private fun isCustomReadRequest(message: Message) =
        message.entities?.any { it.type == MessageEntity.Types.HASHTAG.type } ?: false

    private suspend fun onCustomRead(message: Message) {

        val text = message
            .takeIf { userService.isCustomReadAllowed(it.chat.id) }
            ?.text
            ?: return

        message.entities
            ?.filter { it.type == MessageEntity.Types.HASHTAG.type }
            ?.map { text.substring(it.offset + 1, it.offset + it.length).toUpperCase() }
            ?.distinct()
            ?.forEach {
                telegramSendService.sendChart(message.chat.id, it)
                    .onFailure { exception ->
                        telegramSendService.sendMessage(message.chat.id, exception.getMessage())
                    }
            }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
