package com.majidbahmani.lisbonav.feature.map.domain.usecase

import com.majidbahmani.lisbonav.feature.map.domain.model.GeoPoint
import com.majidbahmani.lisbonav.feature.map.domain.model.Vehicle
import com.majidbahmani.lisbonav.feature.map.domain.model.VehicleStatus
import com.majidbahmani.lisbonav.feature.map.fake.FakeVehicleRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException

class GetVehiclesUseCaseTest {

    /** The feed's time in these tests: most vehicles reported at this moment. */
    private val feedTime = Instant.fromEpochMilliseconds(1791156049000)
    private val repository = FakeVehicleRepository()
    private val getVehicles = GetVehiclesUseCase(repository)

    /** A vehicle that reported [behind] before the feed time (negative = ahead). */
    private fun vehicle(id: String, behind: Duration = Duration.ZERO) = Vehicle(
        id = id,
        lineId = "1997",
        position = GeoPoint(38.7, -9.1),
        bearingDegrees = null,
        speedKmh = null,
        status = VehicleStatus.UNKNOWN,
        stopId = null,
        updatedAt = feedTime - behind,
    )

    private suspend fun visibleIds() = getVehicles().getOrThrow().map { it.id }

    @Test
    fun invoke_keepsVehiclesReportingNormally() = runTest {
        repository.result = Result.success(listOf(vehicle("a"), vehicle("b", 15.seconds), vehicle("c", 71.seconds)))

        assertEquals(listOf("a", "b", "c"), visibleIds())
        assertEquals(1, repository.getVehiclesCalls)
    }

    @Test
    fun invoke_hidesVehicleFarBehindTheOthers() = runTest {
        repository.result = Result.success(listOf(vehicle("a"), vehicle("ghost", 6.minutes), vehicle("b")))

        assertEquals(listOf("a", "b"), visibleIds())
    }

    @Test
    fun invoke_keepsVehicleExactlyAtMaxLag() = runTest {
        repository.result = Result.success(
            listOf(vehicle("a"), vehicle("edge", GetVehiclesUseCase.DEFAULT_MAX_LAG), vehicle("b")),
        )

        assertEquals(listOf("a", "edge", "b"), visibleIds())
    }

    @Test
    fun invoke_oneVehicleWithClockFarAhead_doesNotHideTheOthers() = runTest {
        // With "newest report" as reference, this one bus would make all others look 1 h old.
        repository.result = Result.success(listOf(vehicle("a"), vehicle("b"), vehicle("wrong-clock", (-1).hours)))

        assertEquals(listOf("a", "b", "wrong-clock"), visibleIds())
    }

    @Test
    fun invoke_wholeFeedOld_keepsAllVehicles() = runTest {
        // No phone clock involved: the vehicles are only compared with each other.
        repository.result = Result.success(listOf(vehicle("a", 2.hours), vehicle("b", 2.hours)))

        assertEquals(listOf("a", "b"), visibleIds())
    }

    @Test
    fun invoke_emptyList_returnsEmptyList() = runTest {
        assertTrue(visibleIds().isEmpty())
    }

    @Test
    fun invoke_failure_isPassedThrough() = runTest {
        val error = IOException("offline")
        repository.result = Result.failure(error)

        assertSame(error, getVehicles().exceptionOrNull())
    }
}
