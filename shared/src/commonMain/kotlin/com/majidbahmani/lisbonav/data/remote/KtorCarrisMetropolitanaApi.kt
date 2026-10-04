package com.majidbahmani.lisbonav.data.remote

import com.majidbahmani.lisbonav.data.remote.dto.VehicleDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/** Ktor implementation; [client] must be configured by [createHttpClient] (JSON + base URL). */
class KtorCarrisMetropolitanaApi(
    private val client: HttpClient,
) : CarrisMetropolitanaApi {

    override suspend fun getVehicles(): List<VehicleDto> = client.get("vehicles").body()
}
