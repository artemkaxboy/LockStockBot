package com.artemkaxboy.telerest.tool

import kotlin.reflect.KClass

fun <T : Enum<*>> KClass<T>.valueOfOrNull(value: String): T? {
    return java.takeIf { it.isEnum }
        ?.enumConstants
        ?.firstOrNull { it.name.equals(value, ignoreCase = true) }
}
