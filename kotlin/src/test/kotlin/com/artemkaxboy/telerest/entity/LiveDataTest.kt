package com.artemkaxboy.telerest.entity

import com.artemkaxboy.telerest.tool.Constants
import com.artemkaxboy.telerest.tool.RandomUtils
import com.artemkaxboy.telerest.tool.extensions.round
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class LiveDataTest {

    @Test
    fun `pass if getRoundedPotential returns rounded data`() {
        val randomData = LiveData.random().copy(potential = RandomUtils.price())
        val expected = randomData.potential?.round(Constants.PERCENT_ROUND_PRECISION)
        assertNotEquals(randomData.potential, expected)

        val actual = randomData.getRoundedPotential()
        assertEquals(expected, actual)
    }

    @Test
    fun `getPotentialDifferenceOrNull() fail to return null`() {
        val noPotentialData = LiveData.random()
        val withPotentialData = noPotentialData.copy(potential = RandomUtils.price())

        assertNull(noPotentialData.getPotentialDifferenceOrNull(null))
        assertNull(noPotentialData.getPotentialDifferenceOrNull(withPotentialData))
        assertNull(withPotentialData.getPotentialDifferenceOrNull(noPotentialData))
        assertNotNull(withPotentialData.getPotentialDifferenceOrNull(withPotentialData))
    }
}
