package com.majidbahmani.lisbonav.feature.map.fake

import com.majidbahmani.lisbonav.feature.map.domain.model.Vehicle
import com.majidbahmani.lisbonav.feature.map.domain.repository.VehicleRepository

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
