package com.artemkaxboy.telerest.event

import com.artemkaxboy.telerest.entity.LiveData
import org.springframework.context.ApplicationEvent

// @doc https://www.baeldung.com/spring-events

class PotentialChangedEvent(liveData: LiveData, difference: Double) :
    ApplicationEvent(PotentialChangedEventObject(liveData, difference))

class PotentialChangedEventObject(
    val liveData: LiveData,
    val difference: Double
)
