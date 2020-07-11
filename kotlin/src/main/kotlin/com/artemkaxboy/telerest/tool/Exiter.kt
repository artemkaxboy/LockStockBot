package com.artemkaxboy.telerest.tool

import mu.KotlinLogging
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component
import kotlin.system.exitProcess


@Component
class Exiter(
    private val context: ConfigurableApplicationContext
) {

    fun error(message: String, status: Int = 1) {
        logger.error { message }
        exit(status)
    }

    fun exit(status: Int = 1) {
        context.close()
        exitProcess(status)
    }

    companion object {

        private
        val logger = KotlinLogging.logger { }
    }
}
