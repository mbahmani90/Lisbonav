package com.majidbahmani.lisbonav.fake

import com.majidbahmani.lisbonav.data.remote.CarrisMetropolitanaApi
import com.majidbahmani.lisbonav.data.remote.dto.VehicleDto

/** Returns [vehicles], or throws [error] when set; counts calls. */
class FakeCarrisMetropolitanaApi(
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
