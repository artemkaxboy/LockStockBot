package com.artemkaxboy.telerest.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.convert.DurationUnit
import org.springframework.validation.annotation.Validated
import java.time.Duration
import java.time.temporal.ChronoUnit


/** Amount of attempts to initially connect to Telegram API */
private
const val DEFAULT_RECONNECTION_COUNT = 10


/** Default delay between connection attempts */
private
const val DEFAULT_RECONNECTION_DELAY_SECONDS = 3L


@ConfigurationProperties("telegram")
@Validated
class TelegramBotProperties {

    /** Telegram API connection enabled. */
    var enabled = true

    /** Telegram bot API-token. */
    // @NotBlank(message = "Telegram token must be provided. Use config files or env variables.")
    var token: String = ""

    /** Make sense when the bot added to chat where another bot has the same command,
     *  in this case users should send the command with the bot name before (e.g. @bot1/start). */
    // @NotBlank(message = "Telegram bot name must be provided. Use config files or env variables.")
    var botName: String = ""

    val reconnection = Reconnection()

    class Reconnection {

        /** Amount of attempts to initially connect to Telegram API. */
        var count = DEFAULT_RECONNECTION_COUNT

        /** Delay between connection attempts. */
        @DurationUnit(ChronoUnit.SECONDS)
        var delay: Duration = Duration.ofSeconds(DEFAULT_RECONNECTION_DELAY_SECONDS)
    }
}
