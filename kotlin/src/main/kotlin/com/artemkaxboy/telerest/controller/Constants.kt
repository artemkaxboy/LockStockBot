package com.artemkaxboy.telerest.controller

/**
 * Public constants object. It cannot be used without object due to trouble with using
 * constants in test classes with the same package name. Tests unexpectedly lose constants
 * without import, on the other side using imports IDEA shows warning that this import is unused
 * and suggest to remove it.
 */
object Constants {
    /**
     * Maximum API supported value, bytes representation of 1GB.
     */
    const val MAX_API_INT = 1073741824L

    const val MAX_PAGE_SIZE = 100L

    const val DEFAULT_PAGE_SIZE = 10L
}
