package com.majidbahmani.lisbonav.data.remote

import com.majidbahmani.lisbonav.data.remote.dto.VehicleDto

/** Carris Metropolitana open-data API (https://github.com/carrismetropolitana). No API key needed. */
interface CarrisMetropolitanaApi {

    /** All vehicles currently in service, in one response (the endpoint isn't paginated). */
    suspend fun getVehicles(): List<VehicleDto>

    companion object {
        const val BASE_URL = "https://api.carrismetropolitana.pt/v2/"
    }
}
