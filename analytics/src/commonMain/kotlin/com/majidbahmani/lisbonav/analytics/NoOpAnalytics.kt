package com.majidbahmani.lisbonav.analytics

/** Analytics that does nothing: the default until a platform app passes a real implementation. */
object NoOpAnalytics : Analytics {
    override fun log(event: AnalyticsEvent) = Unit
    override fun setCollectionEnabled(enabled: Boolean) = Unit
}
