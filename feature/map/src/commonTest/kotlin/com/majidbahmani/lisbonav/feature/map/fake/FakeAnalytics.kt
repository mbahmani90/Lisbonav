package com.majidbahmani.lisbonav.feature.map.fake

import com.majidbahmani.lisbonav.analytics.Analytics
import com.majidbahmani.lisbonav.analytics.AnalyticsEvent

/** Records logged events, in order. */
class FakeAnalytics : Analytics {
    val events = mutableListOf<AnalyticsEvent>()
    override fun log(event: AnalyticsEvent) {
        events += event
    }
    override fun setCollectionEnabled(enabled: Boolean) = Unit
}
