package com.majidbahmani.lisbonav.di

import com.majidbahmani.lisbonav.analytics.Analytics

/**
 * Swift entry point (`KoinIosKt.doInitKoinIos(analytics:)`): Kotlin default arguments aren't
 * visible from Swift, so [analytics] is always passed (`NoOpAnalytics.shared` until Firebase).
 */
fun initKoinIos(analytics: Analytics) {
    initKoin(analytics)
}
