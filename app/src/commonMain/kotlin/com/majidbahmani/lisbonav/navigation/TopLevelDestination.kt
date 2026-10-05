package com.majidbahmani.lisbonav.navigation

import com.majidbahmani.lisbonav.feature.transportcard.TransportCardRoute
import com.majidbahmani.lisbonav.feature.map.MapRoute
import com.majidbahmani.lisbonav.resources.Res
import com.majidbahmani.lisbonav.resources.ic_card
import com.majidbahmani.lisbonav.resources.ic_map
import com.majidbahmani.lisbonav.resources.nav_card
import com.majidbahmani.lisbonav.resources.nav_map
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/** The bottom bar tabs: each one is a feature's start destination. */
enum class TopLevelDestination(
    val route: Any,
    val label: StringResource,
    val icon: DrawableResource,
) {
    MAP(route = MapRoute, label = Res.string.nav_map, icon = Res.drawable.ic_map),
    TRANSPORT_CARD(route = TransportCardRoute, label = Res.string.nav_card, icon = Res.drawable.ic_card),
}
