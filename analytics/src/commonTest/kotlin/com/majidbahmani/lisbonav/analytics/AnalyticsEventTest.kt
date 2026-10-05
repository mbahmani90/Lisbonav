package com.majidbahmani.lisbonav.analytics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AnalyticsEventTest {

    @Test
    fun validEvent_keepsNameAndParams() {
        val event = AnalyticsEvent("card_read", mapOf("result" to "success", "count" to 2L, "ratio" to 0.5))

        assertEquals("card_read", event.name)
        assertEquals(mapOf("result" to "success", "count" to 2L, "ratio" to 0.5), event.params)
    }

    @Test
    fun screenView_usesFirebaseScreenParams() {
        assertEquals(
            AnalyticsEvent("screen_view", mapOf("screen_name" to "map", "screen_class" to "map")),
            AnalyticsEvent.screenView("map"),
        )
    }

    @Test
    fun invalidNames_areRejected() {
        listOf("", "1st_event", "card-read", "card read", "a".repeat(41)).forEach { name ->
            assertFailsWith<IllegalArgumentException>(name) { AnalyticsEvent(name) }
        }
    }

    @Test
    fun reservedPrefixes_areRejected() {
        listOf("firebase_x", "google_x", "ga_x").forEach { name ->
            assertFailsWith<IllegalArgumentException>(name) { AnalyticsEvent(name) }
            assertFailsWith<IllegalArgumentException>(name) { AnalyticsEvent("event", mapOf(name to "x")) }
        }
    }

    @Test
    fun invalidParams_areRejected() {
        assertFailsWith<IllegalArgumentException> { AnalyticsEvent("event", mapOf("bad-key" to "x")) }
        assertFailsWith<IllegalArgumentException> { AnalyticsEvent("event", mapOf("count" to 1)) } // Int, not Long
        assertFailsWith<IllegalArgumentException> { AnalyticsEvent("event", mapOf("flag" to true)) }
        assertFailsWith<IllegalArgumentException> { AnalyticsEvent("event", mapOf("text" to "x".repeat(101))) }
        assertFailsWith<IllegalArgumentException> {
            AnalyticsEvent("event", (1..26).associate { "p$it" to "x" })
        }
    }

    @Test
    fun limits_areInclusive() {
        AnalyticsEvent("a".repeat(40), (1..25).associate { "p$it" to "x".repeat(100) })
    }
}
