package com.majidbahmani.lisbonav.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.majidbahmani.lisbonav.domain.usecase.GetVehiclesUseCase
import com.majidbahmani.lisbonav.presentation.viewmodel.VehicleMapUiState.ErrorReason
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.io.IOException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Polls the vehicles while the map is visible.
 *
 * - Refreshes every [refreshInterval]; [retry] refreshes right away.
 * - Polling stops [STOP_TIMEOUT] after the UI stops collecting (app in background) and starts
 *   again when it comes back; the last vehicles stay in the state meanwhile.
 */
class VehicleMapViewModel(
    private val getVehicles: GetVehiclesUseCase,
    private val refreshInterval: Duration = DEFAULT_REFRESH_INTERVAL,
) : ViewModel() {

    private val retryTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    // Survives a stop/restart of the polling below, so the map keeps its buses.
    private var lastState = VehicleMapUiState()

    val uiState: StateFlow<VehicleMapUiState> = flow {
        while (true) {
            lastState = getVehicles().fold(
                onSuccess = { vehicles -> lastState.copy(vehicles = vehicles, isLoading = false, error = null) },
                // Keep the buses from the last success; only report the error.
                onFailure = { error -> lastState.copy(isLoading = false, error = error.toErrorReason()) },
            )
            emit(lastState)
            awaitNextRefresh()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds),
        initialValue = lastState,
    )

    fun retry() {
        retryTrigger.tryEmit(Unit)
    }

    /** Waits for the next interval, or less if [retry] is called. */
    private suspend fun awaitNextRefresh() {
        withTimeoutOrNull(refreshInterval) { retryTrigger.first() }
    }

    private fun Throwable.toErrorReason(): ErrorReason = when (this) {
        // Offline, DNS, timeouts (OkHttp and Darwin errors are IOExceptions).
        is IOException -> ErrorReason.NO_CONNECTION
        // HTTP errors, malformed responses.
        else -> ErrorReason.SERVICE
    }

    companion object {
        /** The API caches responses for 5 s, so polling faster brings nothing new. */
        val DEFAULT_REFRESH_INTERVAL: Duration = 10.seconds

        /** Long enough to survive a screen rotation without restarting the polling. */
        val STOP_TIMEOUT: Duration = 5.seconds
    }
}
