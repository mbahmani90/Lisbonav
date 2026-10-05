package com.majidbahmani.lisbonav.feature.map

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.majidbahmani.lisbonav.feature.map.presentation.ui.map.VehicleMapRoute
import kotlinx.serialization.Serializable

/** The map's destination. The feature owns its route; the app only adds it to the NavHost. */
@Serializable
data object MapRoute

/** The map feature's entry point for the app's NavHost. */
fun NavGraphBuilder.mapScreen() {
    composable<MapRoute> {
        VehicleMapRoute()
    }
}
