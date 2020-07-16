package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.config.TelegramBotConfig
import com.artemkaxboy.telerest.config.properties.TelegramBotProperties
import com.artemkaxboy.telerest.tool.getOutput
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.system.measureTimeMillis

internal class TelegramBotTest {

    @Test
    fun `fail to disable bot`() {

        val botProperties = TelegramBotProperties().apply {
            enabled = false
            token = "DUMMY_TOKEN"
        }

        val bot = TelegramBotConfig(botProperties).getTelegramBot()

        val (_, out, _) = getOutput {
            runBlocking {
                bot.start()
            }
        }

        Assertions.assertThat(bot.started).isFalse()
        Assertions.assertThat(out).contains("Telegram bot disabled.")
    }

    @Test
    fun `fail to disable bot by empty token`() {

        val botProperties = TelegramBotProperties().apply {
            enabled = true
            token = ""
        }

        val bot = TelegramBotConfig(botProperties).getTelegramBot()

        val (_, out, _) = getOutput {
            runBlocking {
                bot.start()
            }
        }

        Assertions.assertThat(bot.started).isFalse()
        Assertions.assertThat(out).contains("Telegram token is empty.")
        Assertions.assertThat(out).contains("Telegram bot disabled.")
    }

    @Test
    fun `fail to reconnect on startup error`() {

        val botProperties = TelegramBotProperties().apply {
            reconnection.count = 3
            reconnection.delay = Duration.ofMillis(10)
            token = "DUMMY_TOKEN"
        }

        val bot = TelegramBotConfig(botProperties).getTelegramBot()

        val (_, out, _) = getOutput {
            val errorMessage = runBlocking {
                bot.start()
            }

            Assertions.assertThat(errorMessage).isNotNull()
        }

        Assertions.assertThat(bot.started).isFalse()

        out.split("\n")
            .count { it.contains("bot starting...") }
            .also {
                Assertions.assertThat(it).isEqualTo(botProperties.reconnection.count)
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

        val bot = TelegramBotConfig(botProperties).getTelegramBot()

        val timeSpent = measureTimeMillis {
            val errorMessage = runBlocking {
                bot.start()
            }

            Assertions.assertThat(errorMessage).isNotNull()
        }

        Assertions.assertThat(timeSpent)
            .isGreaterThan(botProperties.reconnection.let { it.count * it.delay.toMillis() })
    }
}
