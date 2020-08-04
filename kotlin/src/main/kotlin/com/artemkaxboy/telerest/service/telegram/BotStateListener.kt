package com.artemkaxboy.telerest.service.telegram

import com.elbekD.bot.Bot

interface BotStateListener {

    /**
     * Call when bot state has changed.
     *
     * @param bot which state has changed.
     * @param state new state.
     */
    fun onStateChanged(bot: Bot, state: BotState)
}

/**
 * Bot states
 */
enum class BotState {

    /** Default status after start the app. */
    OFF,

    /** Bot has been created but has not been started. */
    CREATED,

    /** Bot has been started. */
    STARTED
}
