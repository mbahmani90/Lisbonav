package com.majidbahmani.lisbonav.feature.map.data.mapper

import com.majidbahmani.lisbonav.feature.map.data.remote.dto.VehicleDto
import com.majidbahmani.lisbonav.feature.map.domain.model.GeoPoint
import com.majidbahmani.lisbonav.feature.map.domain.model.Vehicle
import com.majidbahmani.lisbonav.feature.map.domain.model.VehicleStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class VehicleMapperTest {

    private fun dto(
        id: String = "[LA77N]1314",
        lat: Double = 38.686607,
        lon: Double = -9.332203,
        bearing: Int? = 104,
        speed: Int? = 50,
        currentStatus: String? = "STOPPED_AT",
    ) = VehicleDto(
        id = id,
        lat = lat,
        lon = lon,
        timestamp = 1791156049000,
        lineId = "1997",
        bearing = bearing,
        speed = speed,
        stopId = "050050",
        currentStatus = currentStatus,
    )

    @Test
    fun toDomainOrNull_mapsAllFields() {
        assertEquals(
            Vehicle(
                id = "[LA77N]1314",
                lineId = "1997",
                position = GeoPoint(latitude = 38.686607, longitude = -9.332203),
                bearingDegrees = 104,
                speedKmh = 50,
                status = VehicleStatus.STOPPED_AT,
                stopId = "050050",
                updatedAt = Instant.fromEpochMilliseconds(1791156049000),
            ),
            dto().toDomainOrNull(),
        )
    }

    @Test
    fun toDomainOrNull_nullIslandPosition_returnsNull() {
        // Seen in live data: a vehicle without GPS reports lat 0, lon 0.
        assertNull(dto(lat = 0.0, lon = 0.0).toDomainOrNull())
    }

    @Test
    fun toDomainOrNull_outOfRangeCoordinates_returnsNull() {
        assertNull(dto(lat = 91.0).toDomainOrNull())
        assertNull(dto(lon = -181.0).toDomainOrNull())
    }

    @Test
    fun toDomainOrNull_mapsStatuses_unknownForMissingOrNewValues() {
        assertEquals(VehicleStatus.INCOMING_AT, dto(currentStatus = "INCOMING_AT").toDomainOrNull()?.status)
        assertEquals(VehicleStatus.IN_TRANSIT_TO, dto(currentStatus = "IN_TRANSIT_TO").toDomainOrNull()?.status)
        assertEquals(VehicleStatus.UNKNOWN, dto(currentStatus = null).toDomainOrNull()?.status)
        assertEquals(VehicleStatus.UNKNOWN, dto(currentStatus = "SOMETHING_NEW").toDomainOrNull()?.status)
    }

    @Test
    fun toDomainOrNull_normalizesBearing() {
        assertEquals(0, dto(bearing = 360).toDomainOrNull()?.bearingDegrees)
        assertNull(dto(bearing = 400).toDomainOrNull()?.bearingDegrees)
        assertNull(dto(bearing = -5).toDomainOrNull()?.bearingDegrees)
        assertNull(dto(bearing = null).toDomainOrNull()?.bearingDegrees)
    }

    @Test
    fun toDomainOrNull_negativeSpeed_isUnknown() {
        assertNull(dto(speed = -1).toDomainOrNull()?.speedKmh)
    }

    @Test
    fun listToDomain_dropsUnusableVehicles_keepsOrder() {
        val vehicles = listOf(
            dto(id = "a"),
            dto(id = "no-gps", lat = 0.0, lon = 0.0),
            dto(id = "b"),
        ).toDomain()

        assertEquals(listOf("a", "b"), vehicles.map { it.id })
    }
}
