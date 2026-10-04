package com.majidbahmani.lisbonav.data.remote

import com.majidbahmani.lisbonav.data.remote.dto.VehicleDto
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KtorCarrisMetropolitanaApiTest {

    private val requests = mutableListOf<HttpRequestData>()

    private fun api(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ) = KtorCarrisMetropolitanaApi(
        createHttpClient(
            MockEngine { request ->
                requests += request
                handler(request)
            },
        ),
    )

    private fun MockRequestHandleScope.respondJson(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

    @Test
    fun getVehicles_requestsVehiclesEndpoint() = runTest {
        api { respondJson("[]") }.getVehicles()

        val request = requests.single()
        assertEquals(HttpMethod.Get, request.method)
        assertEquals("https://api.carrismetropolitana.pt/v2/vehicles", request.url.toString())
    }

    @Test
    fun getVehicles_parsesRealResponse_andSkipsUnknownFields() = runTest {
        val vehicles = api { respondJson(VEHICLES_JSON_SAMPLE) }.getVehicles()

        assertEquals(2, vehicles.size)
        assertEquals(
            VehicleDto(
                id = "[LA77N]1314",
                lat = 38.686607,
                lon = -9.332203,
                timestamp = 1791156049000,
                lineId = "1997",
                bearing = 104,
                speed = 50,
                agencyId = "41",
                routeId = "[LA77N]1997_0",
                patternId = "[VNWG3][LA77N]1997_0_2",
                tripId = "[VNWG3][LA77N]1997_0_2_2400_2429_0_73",
                stopId = "050050",
                directionId = 1,
                currentStatus = "STOPPED_AT",
                doorStatus = "CLOSED",
                occupancyStatus = "NO_DATA_AVAILABLE",
                scheduleRelationship = "SCHEDULED",
                bikesAllowed = false,
                contactless = true,
            ),
            vehicles.first(),
        )
    }

    @Test
    fun getVehicles_acceptsNullBearing() = runTest {
        val vehicle = api { respondJson(VEHICLES_JSON_SAMPLE) }.getVehicles()[1]

        assertEquals("[BNA17]1211", vehicle.id)
        assertNull(vehicle.bearing)
    }

    @Test
    fun getVehicles_emptyArray_returnsEmptyList() = runTest {
        assertTrue(api { respondJson("[]") }.getVehicles().isEmpty())
    }

    @Test
    fun getVehicles_serverError_throws() = runTest {
        assertFailsWith<ServerResponseException> {
            api { respondJson("""{"error":"down"}""", HttpStatusCode.InternalServerError) }.getVehicles()
        }
    }
}
