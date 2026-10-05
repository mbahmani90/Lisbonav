package com.majidbahmani.lisbonav.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/** [Analytics] backed by Firebase. Collection starts off (manifest) until consent. */
class FirebaseAndroidAnalytics(private val firebase: FirebaseAnalytics) : Analytics {

    override fun log(event: AnalyticsEvent) {
        firebase.logEvent(event.name, event.params.toBundle())
    }

    override fun setCollectionEnabled(enabled: Boolean) {
        firebase.setAnalyticsCollectionEnabled(enabled)
    }
}

/** AnalyticsEvent only accepts String, Long and Double values. */
private fun Map<String, Any>.toBundle() = Bundle().apply {
    forEach { (key, value) ->
        when (value) {
            is String -> putString(key, value)
            is Long -> putLong(key, value)
            is Double -> putDouble(key, value)
        }
    }
}
