package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.config.properties.TelegramBotProperties
import com.artemkaxboy.telerest.service.telegram.BotState
import com.artemkaxboy.telerest.service.telegram.TelegramService
import com.artemkaxboy.telerest.tool.getOutput
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.assertj.core.api.Assertions as Assertjions
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.system.measureTimeMillis

internal class TelegramServiceTest {

    @Test
    fun `fail to disable bot`() {

        val botProperties = TelegramBotProperties().apply {
            enabled = false
            token = "DUMMY_TOKEN"
        }

        val bot = TelegramService(botProperties, mockk())

        val (_, out, _) = getOutput {
            runBlocking {
                bot.startIfNeeded()
            }
        }

        Assertions.assertEquals(BotState.OFF, bot.state)
        Assertjions.assertThat(out).contains("Telegram bot disabled.")
    }

    @Test
    fun `fail to disable bot by empty token`() {

        val botProperties = TelegramBotProperties().apply {
            enabled = true
            token = ""
        }

        val bot = TelegramService(botProperties, mockk())

        val (_, out, _) = getOutput {
            runBlocking {
                bot.startIfNeeded()
            }
        }

        Assertions.assertEquals(BotState.OFF, bot.state)
        Assertjions.assertThat(out).contains("Telegram token is empty.")
        Assertjions.assertThat(out).contains("Telegram bot disabled.")
    }

    @Test
    fun `fail to reconnect on startup error`() {

        val botProperties = TelegramBotProperties().apply {
            reconnection.count = 3
            reconnection.delay = Duration.ofMillis(10)
            token = "DUMMY_TOKEN"
        }

        val bot = TelegramService(botProperties, mockk())

        val (_, out, _) = getOutput {
            val result = runBlocking {
                bot.start()
            }

            Assertjions.assertThat(result.isFailure()).isTrue()
        }

        Assertions.assertEquals(BotState.CREATED, bot.state)

        out.split("\n")
            .count { it.contains("bot starting...") }
            .also {
                Assertjions.assertThat(it).isEqualTo(botProperties.reconnection.count)
                    .`as`("No as reconnection messages as attempts")
            }
    }

    @Test
    fun `fail to wait on reconnection`() {

        val botProperties = TelegramBotProperties().apply {
            reconnection.count = 3
            reconnection.delay = Duration.ofMillis(100)
            token = "DUMMY_TOKEN"
        }

        val bot = TelegramService(botProperties, mockk())

        val timeSpent = measureTimeMillis {
            val result = runBlocking {
                bot.start()
            }

            Assertjions.assertThat(result.isFailure()).isTrue()
        }

        Assertjions.assertThat(timeSpent)
            .isGreaterThan(botProperties.reconnection.let { it.count * it.delay.toMillis() })
    }
}
