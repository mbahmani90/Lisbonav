package com.majidbahmani.lisbonav.feature.map.domain.repository

import com.majidbahmani.lisbonav.feature.map.domain.model.Vehicle

interface VehicleRepository {

    /**
     * The vehicles currently in service that can be shown on a map.
     * Failures (no connection, server error, malformed response) are returned, not thrown.
     */
    suspend fun getVehicles(): Result<List<Vehicle>>
}
