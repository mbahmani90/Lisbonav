package com.majidbahmani.lisbonav.feature.map.domain.usecase

import com.majidbahmani.lisbonav.feature.map.domain.model.Vehicle
import com.majidbahmani.lisbonav.feature.map.domain.repository.VehicleRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * The vehicles to show on the map.
 *
 * Business rule: hide "ghost" buses, i.e. vehicles whose position is more than [maxLag] behind
 * the rest of the feed. A live bus reports every few seconds, so a bus far behind the others
 * has stopped reporting.
 *
 * The age is measured against the feed's own time (the median report time), not the phone's
 * clock: a phone with the wrong time would otherwise hide every bus. The median, not the newest
 * report, so one bus with a wrong clock far ahead can't make all the others look old.
 */
class GetVehiclesUseCase(
    private val repository: VehicleRepository,
    private val maxLag: Duration = DEFAULT_MAX_LAG,
) {
    suspend operator fun invoke(): Result<List<Vehicle>> =
        repository.getVehicles().map { vehicles ->
            if (vehicles.isEmpty()) return@map vehicles
            val feedTime = vehicles.map { it.updatedAt }.sorted()[vehicles.size / 2]
            vehicles.filter { it.updatedAt >= feedTime - maxLag }
        }

    companion object {
        val DEFAULT_MAX_LAG: Duration = 5.minutes
    }
}
