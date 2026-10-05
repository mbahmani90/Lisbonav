package com.majidbahmani.lisbonav.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.majidbahmani.lisbonav.analytics.Analytics
import com.majidbahmani.lisbonav.analytics.AnalyticsEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject

/**
 * Logs a `screen_view` each time another tab is shown. Firebase can't see Compose screens (one
 * Activity / view controller hosts them all, and its automatic screen reporting is off), so the
 * NavHost's back stack is the one place that knows the current screen.
 */
@Composable
internal fun LogScreenViews(navController: NavHostController, analytics: Analytics = koinInject()) {
    LaunchedEffect(navController, analytics) {
        navController.currentBackStackEntryFlow
            .map { entry -> TopLevelDestination.of(entry.destination)?.screenName }
            .toScreenViews()
            .collect { analytics.log(it) }
    }
}

/**
 * One `screen_view` per change of screen: the same screen again (recomposition, re-selecting the
 * tab) isn't logged twice, and destinations without a screen name (null) are skipped.
 */
internal fun Flow<String?>.toScreenViews(): Flow<AnalyticsEvent> = filterNotNull()
    .distinctUntilChanged()
    .map { AnalyticsEvent.screenView(it) }
