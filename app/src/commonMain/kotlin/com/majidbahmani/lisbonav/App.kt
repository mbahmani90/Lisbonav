package com.majidbahmani.lisbonav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.majidbahmani.lisbonav.feature.consent.presentation.ui.AnalyticsConsentDialog
import com.majidbahmani.lisbonav.feature.map.MapRoute
import com.majidbahmani.lisbonav.feature.map.mapScreen
import com.majidbahmani.lisbonav.feature.transportcard.transportCardScreen
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
            // Screens fill the whole window and draw behind the floating bar; the padding tells them
            // how much space the bar takes, so their content (map logo, last list item) stays visible.
            val contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding())
            NavHost(navController = navController, startDestination = MapRoute) {
                mapScreen(contentPadding)
                transportCardScreen(contentPadding)
            }
        }

        // Asks once, over the first screen; nothing once the user has answered.
        AnalyticsConsentDialog()
    }
}

@Composable
private fun LisbonavBottomBar(navController: NavHostController) {
    // The selected tab comes from the back stack: no extra screen state to keep in sync.
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination

    // A floating pill: centred, away from the screen edges, above the system navigation bar.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = CircleShape,
            // Slightly transparent, like the search bar: the map shows through.
            color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.7f),
            shadowElevation = 8.dp,
        ) {
            // Custom items, not Material's NavigationBar: that one is at least 80 dp high.
            Row(
                modifier = Modifier.selectableGroup().padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                TopLevelDestination.entries.forEach { destination ->
                    BottomBarItem(
                        destination = destination,
                        selected = currentDestination?.hierarchy?.any { it.hasRoute(destination.route::class) } == true,
                        onClick = {
                            navController.navigate(destination.route) {
                                // One copy of each tab; switching back restores its state (map position, search).
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        }
    }
}

/** A compact tab: a small icon in the yellow indicator when selected, and its label below. */
@Composable
private fun BottomBarItem(
    destination: TopLevelDestination,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .width(80.dp)
            .clip(CircleShape)
            .selectable(selected = selected, onClick = onClick, role = Role.Tab)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 26.dp)
                .background(if (selected) colors.secondaryContainer else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(destination.icon),
                contentDescription = null, // the label below names the tab
                modifier = Modifier.size(18.dp),
                tint = if (selected) colors.onSecondaryContainer else colors.onSurfaceVariant,
            )
        }
        Text(
            text = stringResource(destination.label),
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) colors.onSurface else colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}
