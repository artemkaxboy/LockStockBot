package com.artemkaxboy.telerest.controller

const val BASE_URL = "/api/v1"
const val LIVE_DATA_TICKER_URL = "$BASE_URL/liveData/{ticker}"
const val LIVE_DATA_URL = "$BASE_URL/liveData"

const val JSON_DATA_PATH = "\$.data"
const val JSON_ITEMS_PATH = "$JSON_DATA_PATH.items"
@Deprecated("See LiveDataControllerTest.kt")
const val JSON_FIRST_ITEM_PATH = "$JSON_ITEMS_PATH[0]"

@Deprecated("See LiveDataControllerTest.kt")
const val JSON_ERROR_PATH = "\$.error" // todo get rid of it
