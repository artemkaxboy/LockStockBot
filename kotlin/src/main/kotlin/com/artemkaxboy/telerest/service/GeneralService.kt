package com.artemkaxboy.telerest.service

import com.artemkaxboy.telerest.config.properties.ApplicationProperties
import org.springframework.stereotype.Service

@Service
class GeneralService(private val applicationProperties: ApplicationProperties) {

    /** Returns application version. */
    fun getVersion() = applicationProperties.version
}
