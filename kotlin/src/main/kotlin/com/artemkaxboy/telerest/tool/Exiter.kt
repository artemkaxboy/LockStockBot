package com.artemkaxboy.telerest.tool

import kotlin.system.exitProcess
import mu.KotlinLogging
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component

@Component
class Exiter(
    private val context: ConfigurableApplicationContext
) {

    fun error(message: String, status: Int = 1) {
        logger.error { message }
        logger.error { "Shutting down..." }
        exit(status)
    }

    private fun exit(status: Int = 1) {
        context.close()
        exitProcess(status)
    }

    companion object {

        private val logger = KotlinLogging.logger { }
    }
}
