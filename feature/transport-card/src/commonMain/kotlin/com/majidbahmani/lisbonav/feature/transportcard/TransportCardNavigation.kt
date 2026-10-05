package com.majidbahmani.lisbonav.feature.transportcard

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.majidbahmani.lisbonav.feature.transportcard.presentation.ui.TransportCardRoute
import kotlinx.serialization.Serializable

/** The transport card's destination. The feature owns its route; the app only adds it to the NavHost. */
@Serializable
data object TransportCardRoute

/**
 * The transport card feature's entry point for the app's NavHost.
 * [contentPadding]: space taken by the app's floating bars, so the content can scroll clear of them.
 */
fun NavGraphBuilder.transportCardScreen(contentPadding: PaddingValues = PaddingValues()) {
    composable<TransportCardRoute> {
        TransportCardRoute(contentPadding = contentPadding)
    }
}
