package com.majidbahmani.lisbonav.feature.map.fake

import com.majidbahmani.lisbonav.feature.map.data.remote.CarrisMetropolitanaApi
import com.majidbahmani.lisbonav.feature.map.data.remote.dto.VehicleDto

/** Returns [vehicles], or throws [error] when set; counts calls. */
internal class FakeCarrisMetropolitanaApi(
    var vehicles: List<VehicleDto> = emptyList(),
    var error: Throwable? = null,
) : CarrisMetropolitanaApi {

    var getVehiclesCalls = 0
        private set

    override suspend fun getVehicles(): List<VehicleDto> {
        getVehiclesCalls++
        error?.let { throw it }
        return vehicles
    }
}
