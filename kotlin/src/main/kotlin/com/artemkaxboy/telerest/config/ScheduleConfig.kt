package com.artemkaxboy.telerest.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@ConditionalOnProperty(value = ["application.scheduling-enabled"], havingValue = "true", matchIfMissing = true)
@Configuration
@EnableScheduling
class ScheduleConfig
