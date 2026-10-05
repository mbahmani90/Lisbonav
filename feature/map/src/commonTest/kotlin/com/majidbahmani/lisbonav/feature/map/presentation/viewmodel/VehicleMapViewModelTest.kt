package com.majidbahmani.lisbonav.feature.map.presentation.viewmodel

import com.majidbahmani.lisbonav.analytics.AnalyticsEvent
import com.majidbahmani.lisbonav.feature.map.domain.model.GeoPoint
import com.majidbahmani.lisbonav.feature.map.domain.model.Vehicle
import com.majidbahmani.lisbonav.feature.map.domain.model.VehicleStatus
import com.majidbahmani.lisbonav.feature.map.domain.usecase.GetVehiclesUseCase
import com.majidbahmani.lisbonav.feature.map.fake.FakeAnalytics
import com.majidbahmani.lisbonav.feature.map.fake.FakeVehicleRepository
import com.majidbahmani.lisbonav.feature.map.presentation.viewmodel.VehicleMapUiState.ErrorReason
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleMapViewModelTest {

    // viewModelScope runs on Dispatchers.Main; runTest shares this dispatcher's virtual clock.
    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private val repository = FakeVehicleRepository(Result.success(listOf(vehicle("a"))))

    private val analytics = FakeAnalytics()

    // The real use case with a fake repository (doc 22).
    private fun createViewModel() = VehicleMapViewModel(GetVehiclesUseCase(repository), analytics)

    private fun vehicle(id: String, lineId: String = "1997") = Vehicle(
        id = id,
        lineId = lineId,
        position = GeoPoint(38.7, -9.1),
        bearingDegrees = null,
        speedKmh = null,
        status = VehicleStatus.UNKNOWN,
        stopId = null,
        updatedAt = Instant.fromEpochMilliseconds(1791156049000),
    )

    /** The state is lazy: polling only runs while someone collects it (like the UI). */
    private fun TestScope.collect(viewModel: VehicleMapViewModel) =
        backgroundScope.launch { viewModel.uiState.collect {} }

    private val VehicleMapViewModel.vehicleIds get() = uiState.value.vehicles.map { it.id }

    /**
     * Like the UI typing. The query is Compose state: outside a running composition its changes
     * reach snapshotFlow only when apply notifications are sent (the UI's frame clock does this).
     */
    private fun TestScope.type(viewModel: VehicleMapViewModel, text: String) {
        viewModel.onQueryChange(text)
        Snapshot.sendApplyNotifications()
        runCurrent()
    }

    @Test
    fun initialState_isLoadingWithoutVehicles() = runTest {
        assertEquals(VehicleMapUiState(), createViewModel().uiState.value)
    }

    @Test
    fun noCollector_doesNotLoad() = runTest {
        createViewModel()
        advanceTimeBy(60_000)

        assertEquals(0, repository.getVehiclesCalls)
    }

    @Test
    fun firstRefresh_showsVehicles() = runTest {
        val viewModel = createViewModel()
        collect(viewModel)
        runCurrent()

        assertEquals(VehicleMapUiState(vehicles = listOf(vehicle("a")), isLoading = false), viewModel.uiState.value)
        assertEquals(1, repository.getVehiclesCalls)
    }

    @Test
    fun refreshesEveryInterval() = runTest {
        val viewModel = createViewModel()
        collect(viewModel)
        runCurrent()

        advanceTimeBy(9_999)
        runCurrent()
        assertEquals(1, repository.getVehiclesCalls)

        repository.result = Result.success(listOf(vehicle("b")))
        advanceTimeBy(1)
        runCurrent()
        assertEquals(2, repository.getVehiclesCalls)
        assertEquals(listOf("b"), viewModel.vehicleIds)
    }

    @Test
    fun failureAfterSuccess_keepsVehicles_untilNextSuccess() = runTest {
        val viewModel = createViewModel()
        collect(viewModel)
        runCurrent()

        repository.result = Result.failure(IOException("offline"))
        advanceTimeBy(10_001)
        assertEquals(listOf("a"), viewModel.vehicleIds)
        assertEquals(ErrorReason.NO_CONNECTION, viewModel.uiState.value.error)

        repository.result = Result.success(listOf(vehicle("b")))
        advanceTimeBy(10_000)
        assertEquals(listOf("b"), viewModel.vehicleIds)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun nonNetworkFailure_isServiceError() = runTest {
        repository.result = Result.failure(IllegalStateException("HTTP 500"))
        val viewModel = createViewModel()
        collect(viewModel)
        runCurrent()

        assertEquals(
            VehicleMapUiState(isLoading = false, error = ErrorReason.SERVICE),
            viewModel.uiState.value,
        )
    }

    @Test
    fun retry_refreshesImmediately() = runTest {
        repository.result = Result.failure(IOException("offline"))
        val viewModel = createViewModel()
        collect(viewModel)
        runCurrent()

        repository.result = Result.success(listOf(vehicle("a")))
        viewModel.retry()
        runCurrent()

        assertEquals(2, repository.getVehiclesCalls)
        assertEquals(listOf("a"), viewModel.vehicleIds)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun stopsPollingInBackground_andKeepsVehiclesWhenBack() = runTest {
        val viewModel = createViewModel()
        val ui = collect(viewModel)
        runCurrent()

        ui.cancel() // app goes to the background
        advanceTimeBy(60_000)
        assertEquals(1, repository.getVehiclesCalls)
        assertEquals(listOf("a"), viewModel.vehicleIds)

        collect(viewModel) // app comes back
        runCurrent()
        assertEquals(2, repository.getVehiclesCalls)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun query_filtersVehiclesByLine() = runTest {
        repository.result = Result.success(listOf(vehicle("a", lineId = "3510"), vehicle("b", lineId = "1997")))
        val viewModel = createViewModel()
        collect(viewModel)
        runCurrent()

        type(viewModel, "35")

        assertEquals(listOf("a"), viewModel.vehicleIds)
        assertEquals("35", viewModel.query)
    }

    @Test
    fun query_doesNotStartANewRequest() = runTest {
        val viewModel = createViewModel()
        collect(viewModel)
        runCurrent()

        type(viewModel, "1")
        type(viewModel, "19")
        type(viewModel, "")

        assertEquals(1, repository.getVehiclesCalls)
    }

    @Test
    fun query_staysAppliedAfterRefresh() = runTest {
        repository.result = Result.success(listOf(vehicle("a", lineId = "3510"), vehicle("b", lineId = "1997")))
        val viewModel = createViewModel()
        collect(viewModel)
        runCurrent()
        type(viewModel, "1997")

        repository.result = Result.success(listOf(vehicle("c", lineId = "1997"), vehicle("d", lineId = "3510")))
        advanceTimeBy(10_001)

        assertEquals(listOf("c"), viewModel.vehicleIds)
    }

    @Test
    fun clearingQuery_showsAllVehiclesAgain() = runTest {
        repository.result = Result.success(listOf(vehicle("a", lineId = "3510"), vehicle("b", lineId = "1997")))
        val viewModel = createViewModel()
        collect(viewModel)
        runCurrent()

        type(viewModel, "35")
        type(viewModel, "")

        assertEquals(listOf("a", "b"), viewModel.vehicleIds)
    }

    // --- Analytics ---

    private fun loadError(reason: String) = AnalyticsEvent("map_load_error", mapOf("reason" to reason))

    private fun lineSearch(found: Boolean) = AnalyticsEvent("line_search", mapOf("line_found" to "$found"))

    @Test
    fun loadError_isLoggedOnceWhenLoadingStartsFailing_notOnEveryPoll() = runTest {
        repository.result = Result.failure(IOException("offline"))
        collect(createViewModel())
        runCurrent()
        advanceTimeBy(VehicleMapViewModel.DEFAULT_REFRESH_INTERVAL * 3) // three more failed polls
        runCurrent()

        assertEquals(listOf(loadError("no_connection")), analytics.events)
    }

    @Test
    fun loadError_isLoggedAgainAfterRecovering_orWhenTheReasonChanges() = runTest {
        repository.result = Result.failure(IOException("offline"))
        collect(createViewModel())
        runCurrent()

        repository.result = Result.failure(IllegalStateException("HTTP 500"))
        advanceTimeBy(VehicleMapViewModel.DEFAULT_REFRESH_INTERVAL)
        runCurrent()
        repository.result = Result.success(listOf(vehicle("a")))
        advanceTimeBy(VehicleMapViewModel.DEFAULT_REFRESH_INTERVAL)
        runCurrent()
        repository.result = Result.failure(IllegalStateException("HTTP 500"))
        advanceTimeBy(VehicleMapViewModel.DEFAULT_REFRESH_INTERVAL)
        runCurrent()

        assertEquals(listOf(loadError("no_connection"), loadError("service"), loadError("service")), analytics.events)
    }

    @Test
    fun lineSearch_isLoggedOnceTheUserStopsTyping_withOnlyWhetherTheLineWasFound() = runTest {
        repository.result = Result.success(listOf(vehicle("a", lineId = "3510")))
        val viewModel = createViewModel()
        collect(viewModel)
        runCurrent()

        type(viewModel, "3")
        type(viewModel, "35")
        type(viewModel, "351")
        assertEquals(emptyList(), analytics.events) // still typing
        advanceTimeBy(VehicleMapViewModel.SEARCH_SETTLE_TIME + 1.seconds)

        type(viewModel, "9999")
        advanceTimeBy(VehicleMapViewModel.SEARCH_SETTLE_TIME + 1.seconds)

        // No search text in the events: only found / not found.
        assertEquals(listOf(lineSearch(found = true), lineSearch(found = false)), analytics.events)
    }

    @Test
    fun lineSearch_isNotLoggedForAClearedQuery_orBeforeTheBusesHaveLoaded() = runTest {
        val viewModel = createViewModel() // not collected: nothing loaded yet

        type(viewModel, "3510")
        advanceTimeBy(VehicleMapViewModel.SEARCH_SETTLE_TIME + 1.seconds)
        type(viewModel, "")
        advanceTimeBy(VehicleMapViewModel.SEARCH_SETTLE_TIME + 1.seconds)

        assertEquals(emptyList(), analytics.events)
    }
}
