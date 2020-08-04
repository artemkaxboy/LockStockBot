package com.artemkaxboy.telerest.service.telegram

import com.artemkaxboy.telerest.service.UserService
import com.artemkaxboy.telerest.tool.ExceptionUtils
import com.elbekD.bot.Bot
import com.elbekD.bot.types.Message
import com.elbekD.bot.types.MessageEntity
import mu.KotlinLogging
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class TelegramReceiveService(
    private val telegramService: TelegramService,
    private val userService: UserService,
    private val telegramSendService: TelegramSendService
) : BotStateListener {

    @PostConstruct
    fun setupService() {
        telegramService.addStateListener(this)
    }

    override fun onStateChanged(bot: Bot, state: BotState) {
        if (state == BotState.CREATED) {
            bot.apply {
                onMessage(::onAnyMessage)
            }
        }
    }

    // ENTRYPOINT
    private suspend fun onAnyMessage(message: Message) {

        logger.trace { "Got new message: $message" }

        if (isCustomReadRequest(message)) {
            onCustomRead(message)
            return
        }

        logger.info { "Unknown message: $message" }
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
                        telegramSendService.sendMessage(message.chat.id, ExceptionUtils.messageOrEmpty(exception))
                    }
            }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
