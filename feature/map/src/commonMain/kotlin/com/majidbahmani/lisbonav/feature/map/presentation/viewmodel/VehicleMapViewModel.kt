package com.majidbahmani.lisbonav.feature.map.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.majidbahmani.lisbonav.analytics.Analytics
import com.majidbahmani.lisbonav.feature.map.domain.usecase.GetVehiclesUseCase
import com.majidbahmani.lisbonav.feature.map.presentation.analytics.MapAnalyticsEvents
import com.majidbahmani.lisbonav.feature.map.presentation.viewmodel.VehicleMapUiState.ErrorReason
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
 * - [onQueryChange] filters the buses by line. The query joins after the polling, so typing
 *   never starts a new request (doc 29: screen state can't trigger a reload).
 * - Analytics: `map_load_error` when loading starts failing (not on every failed poll), and
 *   `line_search` once the user stops typing (whether the line was found, never the text).
 */
@OptIn(FlowPreview::class)
class VehicleMapViewModel(
    private val getVehicles: GetVehiclesUseCase,
    private val analytics: Analytics,
    private val refreshInterval: Duration = DEFAULT_REFRESH_INTERVAL,
) : ViewModel() {

    private val retryTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /**
     * The search bar text. Compose state, not a StateFlow: a text field must read its value
     * synchronously, or fast typing is lost when an older value comes back through the flow.
     */
    var query: String by mutableStateOf("")
        private set

    // All polled buses (unfiltered). Survives a stop/restart of the polling, so the map keeps its buses.
    private var lastState = VehicleMapUiState()

    private val polledState: Flow<VehicleMapUiState> = flow {
        // On a restart (back from background) show the last buses right away, before the next fetch.
        emit(lastState)
        while (true) {
            val previousError = lastState.error
            lastState = getVehicles().fold(
                onSuccess = { vehicles -> lastState.copy(vehicles = vehicles, isLoading = false, error = null) },
                // Keep the buses from the last success; only report the error.
                onFailure = { error -> lastState.copy(isLoading = false, error = error.toErrorReason()) },
            )
            lastState.error?.takeIf { it != previousError }?.let { analytics.log(MapAnalyticsEvents.mapLoadError(it)) }
            emit(lastState)
            awaitNextRefresh()
        }
    }

    val uiState: StateFlow<VehicleMapUiState> = combine(polledState, snapshotFlow { query }) { polled, query ->
        polled.copy(vehicles = polled.vehicles.filterByLine(query))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT.inWholeMilliseconds),
        initialValue = lastState,
    )

    init {
        // One line_search per search the user settles on, not per keystroke ("3", "35", "351").
        viewModelScope.launch {
            snapshotFlow { query.trim() }
                .debounce(SEARCH_SETTLE_TIME)
                .distinctUntilChanged()
                // Before the first load there's nothing to search in: "not found" would be wrong.
                .filter { line -> line.isNotEmpty() && !lastState.isLoading }
                .map { line -> lastState.vehicles.filterByLine(line).isNotEmpty() }
                .collect { found -> analytics.log(MapAnalyticsEvents.lineSearch(lineFound = found)) }
        }
    }

    fun onQueryChange(query: String) {
        this.query = query
    }

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

        /** A search counts once the query hasn't changed for this long. */
        val SEARCH_SETTLE_TIME: Duration = 2.seconds
    }
}
