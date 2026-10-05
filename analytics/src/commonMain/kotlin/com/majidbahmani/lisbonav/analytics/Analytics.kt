package com.majidbahmani.lisbonav.analytics

/**
 * Where analytics events go. Shared code only knows this interface; the platform apps implement it
 * with Firebase (Kotlin in `androidApp`, Swift in `iosApp`) and pass it to `initKoin`.
 */
interface Analytics {

    /** Records [event]. Does nothing while collection is disabled. */
    fun log(event: AnalyticsEvent)

    /** Consent: `false` means nothing is collected or sent. Collection starts disabled. */
    fun setCollectionEnabled(enabled: Boolean)
}
