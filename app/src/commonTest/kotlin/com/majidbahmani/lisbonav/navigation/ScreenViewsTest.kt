package com.majidbahmani.lisbonav.navigation

import com.majidbahmani.lisbonav.analytics.AnalyticsEvent
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ScreenViewsTest {

    @Test
    fun logsOncePerChangeOfScreen_andSkipsUnnamedDestinations() = runTest {
        val shown = flowOf("map", "map", "card", null, "card", "map")

        assertEquals(
            listOf("map", "card", "map").map { AnalyticsEvent.screenView(it) },
            shown.toScreenViews().toList(),
        )
    }

    @Test
    fun everyTab_hasAValidUniqueScreenName() {
        val names = TopLevelDestination.entries.map { it.screenName }

        assertEquals(listOf("map", "card"), names)
        assertEquals(names.size, names.toSet().size)
        names.forEach { AnalyticsEvent.screenView(it) } // throws if Firebase would drop it
    }
}
