package com.majidbahmani.lisbonav.feature.map.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One live vehicle from Carris Metropolitana `GET /v2/vehicles`.
 *
 * Only the fields needed to place and describe a vehicle are required; a vehicle without them
 * can't be shown on the map. Everything else is nullable so a missing or null value in one
 * vehicle doesn't fail the whole response. Fields that are always null in the API today
 * (make, model, license_plate, …) are left out and skipped by `ignoreUnknownKeys`.
 */
@Serializable
internal data class VehicleDto(
    val id: String,
    val lat: Double,
    val lon: Double,
    /** Epoch milliseconds of the last position update. */
    val timestamp: Long,
    @SerialName("line_id") val lineId: String,
    /** Degrees clockwise from north; null when the vehicle doesn't report a heading. */
    val bearing: Int? = null,
    val speed: Int? = null,
    @SerialName("agency_id") val agencyId: String? = null,
    @SerialName("route_id") val routeId: String? = null,
    @SerialName("pattern_id") val patternId: String? = null,
    @SerialName("trip_id") val tripId: String? = null,
    @SerialName("stop_id") val stopId: String? = null,
    @SerialName("direction_id") val directionId: Int? = null,
    /** GTFS-RT: `INCOMING_AT`, `STOPPED_AT` or `IN_TRANSIT_TO` (relative to [stopId]). */
    @SerialName("current_status") val currentStatus: String? = null,
    @SerialName("door_status") val doorStatus: String? = null,
    @SerialName("occupancy_status") val occupancyStatus: String? = null,
    @SerialName("schedule_relationship") val scheduleRelationship: String? = null,
    @SerialName("bikes_allowed") val bikesAllowed: Boolean? = null,
    val contactless: Boolean? = null,
)
