package com.majidbahmani.lisbonav.data.repository

import com.majidbahmani.lisbonav.data.mapper.toDomain
import com.majidbahmani.lisbonav.data.remote.CarrisMetropolitanaApi
import com.majidbahmani.lisbonav.domain.model.Vehicle
import com.majidbahmani.lisbonav.domain.repository.VehicleRepository
import kotlin.coroutines.cancellation.CancellationException

class VehicleRepositoryImpl(
    private val api: CarrisMetropolitanaApi,
) : VehicleRepository {

    override suspend fun getVehicles(): Result<List<Vehicle>> =
        try {
            Result.success(api.getVehicles().toDomain())
        } catch (e: CancellationException) {
            // Cancellation isn't an error: let the coroutine stop.
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}
