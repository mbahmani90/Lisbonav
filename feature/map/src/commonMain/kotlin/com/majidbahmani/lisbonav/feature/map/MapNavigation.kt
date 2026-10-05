package com.majidbahmani.lisbonav.feature.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.majidbahmani.lisbonav.feature.map.presentation.ui.map.VehicleMapRoute
import kotlinx.serialization.Serializable

/** The map's destination. The feature owns its route; the app only adds it to the NavHost. */
@Serializable
data object MapRoute

/**
 * The map feature's entry point for the app's NavHost.
 * [contentPadding]: space taken by the app's floating bars; the map still draws behind them.
 */
fun NavGraphBuilder.mapScreen(contentPadding: PaddingValues = PaddingValues()) {
    composable<MapRoute> {
        VehicleMapRoute(contentPadding = contentPadding)
    }
}
