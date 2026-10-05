package com.majidbahmani.lisbonav.feature.map.presentation.viewmodel

import com.majidbahmani.lisbonav.feature.map.domain.model.Vehicle

/**
 * Everything the map screen shows. A data class, not Loading/Success/Error: the buses stay on
 * the map while a refresh runs or fails, and an error is shown on top of them.
 */
data class VehicleMapUiState(
    /** The buses to draw, already filtered by the search query (VehicleMapViewModel.query). */
    val vehicles: List<Vehicle> = emptyList(),
    /** True only until the first answer (success or error) arrives. */
    val isLoading: Boolean = true,
    /** Why the last refresh failed; null after a successful one. */
    val error: ErrorReason? = null,
) {
    /** The reason, not a message: the UI picks the text (localizable). */
    enum class ErrorReason { NO_CONNECTION, SERVICE }
}
