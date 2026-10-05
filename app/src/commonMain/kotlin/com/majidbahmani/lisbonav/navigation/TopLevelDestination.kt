package com.majidbahmani.lisbonav.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import com.majidbahmani.lisbonav.feature.map.MapRoute
import com.majidbahmani.lisbonav.feature.transportcard.TransportCardRoute
import com.majidbahmani.lisbonav.resources.Res
import com.majidbahmani.lisbonav.resources.ic_card
import com.majidbahmani.lisbonav.resources.ic_map
import com.majidbahmani.lisbonav.resources.nav_card
import com.majidbahmani.lisbonav.resources.nav_map
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/**
 * The bottom bar tabs: each one is a feature's start destination.
 *
 * @property screenName the `screen_name` in analytics: short and stable (class names change in
 *   release builds), so never renamed once in use.
 */
enum class TopLevelDestination(
    val route: Any,
    val label: StringResource,
    val icon: DrawableResource,
    val screenName: String,
) {
    MAP(route = MapRoute, label = Res.string.nav_map, icon = Res.drawable.ic_map, screenName = "map"),
    TRANSPORT_CARD(
        route = TransportCardRoute,
        label = Res.string.nav_card,
        icon = Res.drawable.ic_card,
        screenName = "card",
    ),
    ;

    companion object {
        /** The tab [destination] belongs to; null for a destination outside the tabs. */
        fun of(destination: NavDestination): TopLevelDestination? =
            entries.firstOrNull { tab -> destination.hierarchy.any { it.hasRoute(tab.route::class) } }
    }
}
