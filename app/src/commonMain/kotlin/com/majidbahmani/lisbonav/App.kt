package com.majidbahmani.lisbonav

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.majidbahmani.lisbonav.feature.transportcard.transportCardScreen
import com.majidbahmani.lisbonav.feature.map.MapRoute
import com.majidbahmani.lisbonav.feature.map.mapScreen
import com.majidbahmani.lisbonav.navigation.TopLevelDestination
import com.majidbahmani.lisbonav.systemdesign.theme.LisbonavTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun App() {
    LisbonavTheme {
        // An object kept in composition (doc 29 allows it): the back stack is the navigation state.
        val navController = rememberNavController()

        Scaffold(
            bottomBar = { LisbonavBottomBar(navController) },
            // Screens handle the status bar themselves (the map draws behind it).
            contentWindowInsets = WindowInsets(0),
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = MapRoute,
                // Only keep screens above the bottom bar; the top stays edge-to-edge.
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            ) {
                mapScreen()
                transportCardScreen()
            }
        }
    }
}

@Composable
private fun LisbonavBottomBar(navController: NavHostController) {
    // The selected tab comes from the back stack: no extra screen state to keep in sync.
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    NavigationBar {
        TopLevelDestination.entries.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        // One copy of each tab; switching back restores its state (map position, search).
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(painterResource(destination.icon), contentDescription = null) },
                label = { Text(stringResource(destination.label)) },
                // Material's default selected label is `secondary` (our yellow): unreadable on the bar.
                colors = NavigationBarItemDefaults.colors(selectedTextColor = MaterialTheme.colorScheme.onSurface),
            )
        }
    }
}
