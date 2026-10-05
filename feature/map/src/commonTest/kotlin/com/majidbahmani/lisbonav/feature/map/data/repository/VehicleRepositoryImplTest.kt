package com.majidbahmani.lisbonav.feature.map.data.repository

import com.majidbahmani.lisbonav.feature.map.data.remote.dto.VehicleDto
import com.majidbahmani.lisbonav.feature.map.fake.FakeCarrisMetropolitanaApi
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class VehicleRepositoryImplTest {

    private val api = FakeCarrisMetropolitanaApi()
    private val repository = VehicleRepositoryImpl(api)

    private fun dto(id: String, lat: Double = 38.7, lon: Double = -9.1) = VehicleDto(
        id = id,
        lat = lat,
        lon = lon,
        timestamp = 1791156049000,
        lineId = "1997",
    )

    @Test
    fun getVehicles_success_returnsMappedVehicles() = runTest {
        api.vehicles = listOf(dto("a"), dto("b"))

        val result = repository.getVehicles()

        assertEquals(listOf("a", "b"), result.getOrThrow().map { it.id })
        assertEquals(1, api.getVehiclesCalls)
    }

    @Test
    fun getVehicles_dropsVehiclesWithoutPosition() = runTest {
        api.vehicles = listOf(dto("a"), dto("no-gps", lat = 0.0, lon = 0.0))

        assertEquals(listOf("a"), repository.getVehicles().getOrThrow().map { it.id })
    }

    @Test
    fun getVehicles_networkError_returnsFailure() = runTest {
        val error = IOException("offline")
        api.error = error

        assertSame(error, repository.getVehicles().exceptionOrNull())
    }

    @Test
    fun getVehicles_malformedResponse_returnsFailure() = runTest {
        // SerializationException is an IllegalArgumentException: it must still become a failure, not crash.
        api.error = SerializationException("bad json")

        assertEquals("bad json", repository.getVehicles().exceptionOrNull()?.message)
    }

    @Test
    fun getVehicles_cancellation_isRethrown() = runTest {
        api.error = CancellationException("screen closed")

        assertFailsWith<CancellationException> { repository.getVehicles() }
    }
}
