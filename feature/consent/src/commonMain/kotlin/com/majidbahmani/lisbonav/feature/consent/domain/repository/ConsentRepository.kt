package com.majidbahmani.lisbonav.feature.consent.domain.repository

import com.majidbahmani.lisbonav.feature.consent.domain.model.AnalyticsConsent
import kotlinx.coroutines.flow.Flow

/** Storage only; the rules are in the use cases. */
interface ConsentRepository {
    /** The saved answer; `null` until the user answers (or when it can't be read). */
    val analyticsConsent: Flow<AnalyticsConsent?>

    suspend fun setAnalyticsConsent(consent: AnalyticsConsent)
}
