package com.majidbahmani.lisbonav.feature.map.presentation.viewmodel

import com.majidbahmani.lisbonav.feature.map.domain.model.GeoPoint
import com.majidbahmani.lisbonav.feature.map.domain.model.Vehicle
import com.majidbahmani.lisbonav.feature.map.domain.model.VehicleStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class VehicleFilterTest {

    private fun vehicle(lineId: String) = Vehicle(
        id = "bus-$lineId",
        lineId = lineId,
        position = GeoPoint(38.7, -9.1),
        bearingDegrees = null,
        speedKmh = null,
        status = VehicleStatus.UNKNOWN,
        stopId = null,
        updatedAt = Instant.fromEpochMilliseconds(1791156049000),
    )

    private val vehicles = listOf(vehicle("3510"), vehicle("3507"), vehicle("1997"), vehicle("4715"))

    private fun lines(query: String) = vehicles.filterByLine(query).map { it.lineId }

    @Test
    fun blankQuery_keepsAll() {
        assertEquals(listOf("3510", "3507", "1997", "4715"), lines(""))
        assertEquals(listOf("3510", "3507", "1997", "4715"), lines("   "))
    }

    @Test
    fun prefix_narrowsStepByStep() {
        assertEquals(listOf("3510", "3507"), lines("3"))
        assertEquals(listOf("3510", "3507"), lines("35"))
        assertEquals(listOf("3510"), lines("351"))
    }

    @Test
    fun matchesFromTheStartOnly() {
        // "97" is inside 1997 but isn't how a line number is typed.
        assertEquals(emptyList(), lines("97"))
    }

    @Test
    fun queryIsTrimmed() {
        assertEquals(listOf("1997"), lines(" 1997 "))
    }

    @Test
    fun noMatch_isEmpty() {
        assertEquals(emptyList(), lines("9999"))
    }
}
