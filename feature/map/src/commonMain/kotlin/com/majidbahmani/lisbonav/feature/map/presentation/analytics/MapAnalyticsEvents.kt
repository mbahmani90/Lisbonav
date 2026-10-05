package com.majidbahmani.lisbonav.feature.map.presentation.analytics

import com.majidbahmani.lisbonav.analytics.AnalyticsEvent
import com.majidbahmani.lisbonav.feature.map.presentation.viewmodel.VehicleMapUiState.ErrorReason

/** The map's analytics events: outcomes only, never search text or positions. */
internal object MapAnalyticsEvents {

    /** A search the user settled on: whether a bus of that line is on the map right now. */
    fun lineSearch(lineFound: Boolean) =
        AnalyticsEvent("line_search", mapOf("line_found" to lineFound.toString()))

    /** The buses couldn't be loaded; a fixed reason code, never the exception message. */
    fun mapLoadError(reason: ErrorReason) = AnalyticsEvent(
        "map_load_error",
        mapOf(
            "reason" to when (reason) {
                ErrorReason.NO_CONNECTION -> "no_connection"
                ErrorReason.SERVICE -> "service"
            },
        ),
    )
}
