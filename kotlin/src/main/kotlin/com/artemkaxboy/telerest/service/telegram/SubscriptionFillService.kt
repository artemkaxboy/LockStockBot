package com.artemkaxboy.telerest.service.telegram

import com.artemkaxboy.telerest.config.properties.TelegramBotProperties
import com.artemkaxboy.telerest.entity.User
import com.artemkaxboy.telerest.service.storage.UserService
import com.artemkaxboy.telerest.service.storage.UserTickerSubscriptionService
import com.artemkaxboy.telerest.service.storage.TickerService
import com.artemkaxboy.telerest.tool.ExceptionUtils.getMessage
import com.artemkaxboy.telerest.tool.getOrElse
import com.artemkaxboy.telerest.tool.paging.SinglePage
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class SubscriptionFillService(
    private val telegramBotProperties: TelegramBotProperties,
    private val subscriptionService: UserTickerSubscriptionService,
    private val tickerService: TickerService,
    private val userService: UserService
) {

    /**
     * Fill database with initial users and subscribe all users on all tickers.
     */
    @EventListener(ApplicationReadyEvent::class)
    fun fillDbWithInitDataIfNeeded() {
        if (telegramBotProperties.mainChatId.isEmpty()) return

        userService.count()
            .getOrElse {
                logger.error {
                    it.getMessage("Cannot count existing users")
                }
                return
            }
            .takeIf { it > 0 }
            ?.let {
                logger.info { "Users already exist, count: $it" }
                return
            }

        logger.info { "Creating default users" }

        subscriptionService.deleteAll()

        val users = telegramBotProperties.mainChatId
            .zip(telegramBotProperties.mainChatName) { chatId, chatName ->
                User(
                    chatId = chatId,
                    name = chatName,
                    commonSubscriptionAllowed = true,
                    customReadAllowed = chatId > 0,
                    customSubscriptionAllowed = chatId > 0
                )
            }
            .also { userService.saveAll(it) }

        tickerService.findAll(SinglePage.unsorted())
            ?.let { subscriptionService.allToAll(users, it) }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
