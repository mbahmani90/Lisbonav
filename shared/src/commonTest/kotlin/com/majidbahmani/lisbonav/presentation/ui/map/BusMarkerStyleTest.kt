package com.majidbahmani.lisbonav.presentation.ui.map

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BusMarkerStyleTest {

    @Test
    fun bearingBucket_noHeading_isNull() {
        assertNull(BusMarkerStyle.bearingBucket(null))
    }

    @Test
    fun bearingBucket_roundsToNearestStep() {
        assertEquals(0, BusMarkerStyle.bearingBucket(0))
        assertEquals(0, BusMarkerStyle.bearingBucket(7))
        assertEquals(15, BusMarkerStyle.bearingBucket(8))
        assertEquals(90, BusMarkerStyle.bearingBucket(95))
        assertEquals(345, BusMarkerStyle.bearingBucket(352))
    }

    @Test
    fun bearingBucket_nearNorth_wrapsToZero() {
        assertEquals(0, BusMarkerStyle.bearingBucket(353))
        assertEquals(0, BusMarkerStyle.bearingBucket(359))
    }

    @Test
    fun bearingBucket_fewDistinctIcons() {
        // 24 headings + "no heading" = at most 25 cached icons per platform.
        assertEquals(24, (0..359).mapNotNull { BusMarkerStyle.bearingBucket(it) }.toSet().size)
    }
}
