package com.majidbahmani.lisbonav.feature.transportcard

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.majidbahmani.lisbonav.feature.transportcard.presentation.ui.TransportCardScreen
import kotlinx.serialization.Serializable

/** The transport card's destination. The feature owns its route; the app only adds it to the NavHost. */
@Serializable
data object TransportCardRoute

/** The transport card feature's entry point for the app's NavHost. */
fun NavGraphBuilder.transportCardScreen() {
    composable<TransportCardRoute> {
        TransportCardScreen()
    }
}
