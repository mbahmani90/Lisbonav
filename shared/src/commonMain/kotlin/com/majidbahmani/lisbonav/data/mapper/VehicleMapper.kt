package com.majidbahmani.lisbonav.data.mapper

import com.majidbahmani.lisbonav.data.remote.dto.VehicleDto
import com.majidbahmani.lisbonav.domain.model.GeoPoint
import com.majidbahmani.lisbonav.domain.model.Vehicle
import com.majidbahmani.lisbonav.domain.model.VehicleStatus
import kotlin.time.Instant

/** Vehicles that can't be placed on the map are dropped (see [toDomainOrNull]). */
fun List<VehicleDto>.toDomain(): List<Vehicle> = mapNotNull { it.toDomainOrNull() }

/**
 * Maps one vehicle, or returns null when its position is unusable.
 * The API sometimes reports `lat: 0, lon: 0` for a vehicle without GPS (seen in live data).
 */
fun VehicleDto.toDomainOrNull(): Vehicle? {
    if (!hasUsablePosition()) return null
    return Vehicle(
        id = id,
        lineId = lineId,
        position = GeoPoint(latitude = lat, longitude = lon),
        // 360 is also north; anything outside 0..360 is treated as unknown.
        bearingDegrees = bearing?.takeIf { it in 0..360 }?.rem(360),
        speedKmh = speed?.takeIf { it >= 0 },
        status = currentStatus.toVehicleStatus(),
        stopId = stopId,
        updatedAt = Instant.fromEpochMilliseconds(timestamp),
    )
}

private fun VehicleDto.hasUsablePosition(): Boolean =
    lat in -90.0..90.0 && lon in -180.0..180.0 && !(lat == 0.0 && lon == 0.0)

private fun String?.toVehicleStatus(): VehicleStatus = when (this) {
    "INCOMING_AT" -> VehicleStatus.INCOMING_AT
    "STOPPED_AT" -> VehicleStatus.STOPPED_AT
    "IN_TRANSIT_TO" -> VehicleStatus.IN_TRANSIT_TO
    else -> VehicleStatus.UNKNOWN
}
