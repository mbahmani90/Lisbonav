package com.majidbahmani.lisbonav.feature.map.data.remote

import com.majidbahmani.lisbonav.feature.map.data.remote.dto.VehicleDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/**
 * Ktor implementation on the shared client from :core (`createHttpClient`: JSON, errors).
 * The client has no base URL, so this API builds its own URLs from [CarrisMetropolitanaApi.BASE_URL].
 */
internal class KtorCarrisMetropolitanaApi(
    private val client: HttpClient,
) : CarrisMetropolitanaApi {

    override suspend fun getVehicles(): List<VehicleDto> =
        client.get("${CarrisMetropolitanaApi.BASE_URL}vehicles").body()
}
