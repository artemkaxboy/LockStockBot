package com.artemkaxboy.telerest.controller

const val BASE_URL = "/api/v1"
const val LIVE_DATA_URL = "$BASE_URL/liveData/{ticker}"

const val JSON_ITEMS_PATH = "\$.data.items"
const val JSON_FIRST_ITEM_PATH = "$JSON_ITEMS_PATH[0]"

const val JSON_ERROR_PATH = "\$.error"
