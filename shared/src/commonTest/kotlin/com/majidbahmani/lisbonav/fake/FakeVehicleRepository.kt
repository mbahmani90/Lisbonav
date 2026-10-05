package com.majidbahmani.lisbonav.fake

import com.majidbahmani.lisbonav.domain.model.Vehicle
import com.majidbahmani.lisbonav.domain.repository.VehicleRepository

/** Returns [result]; counts calls. */
class FakeVehicleRepository(
    var result: Result<List<Vehicle>> = Result.success(emptyList()),
) : VehicleRepository {

    var getVehiclesCalls = 0
        private set

    override suspend fun getVehicles(): Result<List<Vehicle>> {
        getVehiclesCalls++
        return result
    }
}
