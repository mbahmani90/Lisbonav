package com.majidbahmani.lisbonav.domain.model

/** Where the vehicle is relative to its current stop (GTFS-Realtime `VehicleStopStatus`). */
enum class VehicleStatus {
    INCOMING_AT,
    STOPPED_AT,
    IN_TRANSIT_TO,

    /** Missing or a value this app doesn't know yet. */
    UNKNOWN,
}
