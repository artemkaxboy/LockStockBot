package com.artemkaxboy.telerest.tool

import mu.KotlinLogging
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

@Component
class Exiter : ApplicationContextAware {

    lateinit var context: ConfigurableApplicationContext

    fun error(message: String, status: Int = 1) {
        logger.error { message }
        exit(status)
    }

    fun exit(status: Int = 1) {
        context.close()
        exitProcess(status)
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext as ConfigurableApplicationContext
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
