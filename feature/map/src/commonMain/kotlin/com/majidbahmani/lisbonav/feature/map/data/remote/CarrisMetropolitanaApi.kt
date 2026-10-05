package com.majidbahmani.lisbonav.feature.map.data.remote

import com.majidbahmani.lisbonav.feature.map.data.remote.dto.VehicleDto

/** Carris Metropolitana open-data API (https://github.com/carrismetropolitana). No API key needed. */
internal interface CarrisMetropolitanaApi {

    /** All vehicles currently in service, in one response (the endpoint isn't paginated). */
    suspend fun getVehicles(): List<VehicleDto>

    companion object {
        const val BASE_URL = "https://api.carrismetropolitana.pt/v2/"
    }
}
