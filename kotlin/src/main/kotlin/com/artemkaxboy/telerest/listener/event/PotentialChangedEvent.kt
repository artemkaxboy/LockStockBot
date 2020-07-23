package com.artemkaxboy.telerest.listener.event

import com.artemkaxboy.telerest.entity.LiveData
import org.springframework.context.ApplicationEvent

// @doc https://www.baeldung.com/spring-events

class PotentialChangedEvent(liveData: LiveData, yesterdayData: LiveData) :
    ApplicationEvent(
        PotentialChangedEventObject(
            liveData,
            yesterdayData
        )
    )

class PotentialChangedEventObject(
    val liveData: LiveData,
    val yesterdayData: LiveData
)
