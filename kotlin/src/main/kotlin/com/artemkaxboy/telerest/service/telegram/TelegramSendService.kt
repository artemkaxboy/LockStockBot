package com.artemkaxboy.telerest.service.telegram

import com.artemkaxboy.telerest.service.ChartService
import com.artemkaxboy.telerest.tool.ExceptionUtils.getMessage
import com.artemkaxboy.telerest.tool.ExceptionUtils.prefixReason
import com.artemkaxboy.telerest.tool.Result
import com.artemkaxboy.telerest.tool.getOrElse
import com.artemkaxboy.telerest.tool.telegram.MarkupUtils
import com.elbekD.bot.types.CallbackQuery
import com.elbekD.bot.types.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service

@Service
class TelegramSendService(
    private val chartService: ChartService,
    private val telegramService: TelegramService
) {

    suspend fun sendChart(chatId: Long, ticker: String): Result<String> {

        if (ticker.isEmpty()) {
            return Result.failure("Specify ticker. Example: /chart AAPL")
        }

        val chartMessage = chartService.getChartMessage(ticker)
            .getOrElse {
                return Result.failure(it.getMessage("Cannot get chart for $ticker"))
            }

        return chartMessage.generateByteArray()
            .getOrElse {
                return Result.failure(it.getMessage("Cannot generate byte array for $ticker"))
            }
            .let {
                sendPhoto(chatId, it, chartMessage.caption)
            }
    }

    /**
     * Sends text message to given chat.
     *
     * @param text to send.
     * @param sendTo chat ID to send the message to.
     * @return exception safety result of the operation.
     */
    suspend fun sendMessage(sendTo: Long, text: String): Result<Message> {
        val error = "Cannot send message"

        return Result.of(error) {
            val bot = telegramService.getBot()
                .getOrElse { return Result.failure(it.getMessage(error)) }

            withContext(Dispatchers.IO) {
                bot.sendMessage(sendTo, text).get()
            }
        }
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
    suspend fun sendPhoto(sendTo: Long, byteArray: ByteArray, caption: String? = null): Result<String> {
        val error = "Cannot send photo"

        return Result.of(error) {
            val bot = telegramService.getBot()
                .getOrElse { return Result.failure(it.getMessage(error)) }

            val response = withContext(Dispatchers.IO) {
                bot.sendPhoto(sendTo, byteArray, caption).get()
            }

            response.photo?.get(0)?.file_id
                ?: return Result.failure("Cannot get file id from response $response".prefixReason(error))
        }
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
    suspend fun sendPhoto(sendTo: Long, fileId: String, caption: String? = null): Result<String> {
        val error = "Cannot send photo"

        return Result.of(error) {
            val bot = telegramService.getBot()
                .getOrElse { return Result.failure(it.getMessage(error)) }

            val response = withContext(Dispatchers.IO) {
                bot.sendPhoto(sendTo, fileId, caption).get()
            }

            response.photo?.get(0)?.file_id
                ?: return Result.failure("Cannot get file id from response $response".prefixReason(error))
        }
    }

    suspend fun sendListMenu(
        sendTo: Long,
        listData: Page<Pair<String, String>>,
        callbackDataPrefix: String
    ): Result<Message> {
        val error = "Cannot send list menu"

        require(callbackDataPrefix.isNotBlank()) { "Prefix cannot be blank".prefixReason(error) }

        return Result.of(error) {
            val bot = telegramService.getBot()
                .getOrElse { return Result.failure(it.getMessage(error)) }

            withContext(Dispatchers.IO) {

                bot.sendMessage(
                    sendTo,
                    "Choose a ticker",
                    markup = MarkupUtils.getListPageMarkup(listData, callbackDataPrefix)
                ).get()
            }
        }
    }

    suspend fun updateListMenu(
        message: Message,
        listData: Page<Pair<String, String>>,
        callbackDataPrefix: String,
        text: String? = null
    ): Result<Message> {
        val error = "Cannot update list menu"

        return Result.of(error) {
            val bot = telegramService.getBot()
                .getOrElse { return Result.failure(it.getMessage(error)) }

            withContext(Dispatchers.IO) {

                bot.editMessageText(
                    message.chat.id,
                    message.message_id,
                    text = text ?: message.text.orEmpty(),
                    markup = MarkupUtils.getListPageMarkup(listData, callbackDataPrefix)
                ).get()
            }
        }
    }

    suspend fun clearMenu(message: Message): Result<Boolean> {
        val error = "Cannot clear menu"

        return Result.of(error) {
            val bot = telegramService.getBot()
                .getOrElse { return Result.failure(it.getMessage(error)) }

            withContext(Dispatchers.IO) {
                bot.deleteMessage(message.chat.id, message.message_id).get()
            }
        }
    }

    suspend fun ackCallback(callbackQuery: CallbackQuery): Result<Boolean> {
        val error = "Cannot send callback acknowledge"

        return Result.of(error) {
            val bot = telegramService.getBot()
                .getOrElse { return Result.failure(it.getMessage(error)) }

            withContext(Dispatchers.IO) {
                bot.answerCallbackQuery(callbackQuery.id).get()
            }
        }
    }
}
