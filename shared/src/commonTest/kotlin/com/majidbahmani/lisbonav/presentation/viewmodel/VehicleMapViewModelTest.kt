package com.majidbahmani.lisbonav.presentation.viewmodel

import com.majidbahmani.lisbonav.domain.model.GeoPoint
import com.majidbahmani.lisbonav.domain.model.Vehicle
import com.majidbahmani.lisbonav.domain.model.VehicleStatus
import com.majidbahmani.lisbonav.domain.usecase.GetVehiclesUseCase
import com.majidbahmani.lisbonav.fake.FakeVehicleRepository
import com.majidbahmani.lisbonav.presentation.viewmodel.VehicleMapUiState.ErrorReason
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
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleMapViewModelTest {

    // viewModelScope runs on Dispatchers.Main; runTest shares this dispatcher's virtual clock.
    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private val repository = FakeVehicleRepository(Result.success(listOf(vehicle("a"))))

    // The real use case with a fake repository (doc 22).
    private fun createViewModel() = VehicleMapViewModel(GetVehiclesUseCase(repository))

    private fun vehicle(id: String) = Vehicle(
        id = id,
        lineId = "1997",
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
}
