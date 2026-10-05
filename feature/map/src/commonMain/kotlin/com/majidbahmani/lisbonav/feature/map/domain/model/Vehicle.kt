package com.majidbahmani.lisbonav.feature.map.domain.model

import kotlin.time.Instant

/** A bus in service, with what the map needs to draw and describe it. */
data class Vehicle(
    val id: String,
    val lineId: String,
    val position: GeoPoint,
    /** Heading in degrees clockwise from north (0–359); null when unknown. */
    val bearingDegrees: Int?,
    val speedKmh: Int?,
    val status: VehicleStatus,
    /** The stop [status] refers to. */
    val stopId: String?,
    /** When this position was reported. */
    val updatedAt: Instant,
)
