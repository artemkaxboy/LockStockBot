package com.artemkaxboy.telerest.service.telegram

import com.artemkaxboy.telerest.service.ChartService
import com.artemkaxboy.telerest.tool.ExceptionUtils
import com.artemkaxboy.telerest.tool.Result
import com.artemkaxboy.telerest.tool.getOrElse
import com.artemkaxboy.telerest.tool.orElse
import com.elbekD.bot.types.Message
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
            .getOrElse { exception ->

                return Result.failure(
                    ExceptionUtils.messageOrDefault(exception, "Cannot get chart for $ticker: ")
                )
            }

        return chartMessage.generateByteArray()
            .getOrElse { exception ->

                return Result.failure(
                    ExceptionUtils.messageOrDefault(exception, "Cannot generate byte array for $ticker: ")
                )
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
    fun sendMessage(sendTo: Long, text: String): Result<Message> =
        Result.of {
            telegramService.bot?.sendMessage(sendTo, text)?.get()
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
            telegramService.bot?.sendPhoto(sendTo, byteArray, caption)?.get()?.photo?.get(0)?.file_id
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
            telegramService.bot?.sendPhoto(sendTo, fileId, caption)?.get()?.photo?.get(0)?.file_id
        }.orElse {
            Result.failure("Cannot send photo: $it")
        }
}
