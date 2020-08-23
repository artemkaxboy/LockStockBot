package com.artemkaxboy.telerest.listener.event

import com.artemkaxboy.telerest.entity.LiveData
import org.springframework.context.ApplicationEvent

// @doc https://www.baeldung.com/spring-events

class PotentialChangedEvent(val source: LiveData) : ApplicationEvent(source)
