package com.majidbahmani.lisbonav.feature.consent.domain.usecase

import com.majidbahmani.lisbonav.analytics.Analytics
import com.majidbahmani.lisbonav.feature.consent.domain.model.AnalyticsConsent
import com.majidbahmani.lisbonav.feature.consent.domain.repository.ConsentRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Keeps Analytics in line with the saved answer, now and on every change. Runs until cancelled.
 *
 * The rule (GDPR opt-in): collection is on only after an explicit "allow"; no answer yet and
 * "don't allow" both keep it off.
 */
class ApplyAnalyticsConsentUseCase(
    private val repository: ConsentRepository,
    private val analytics: Analytics,
) {
    suspend operator fun invoke() {
        repository.analyticsConsent
            .map { it == AnalyticsConsent.GRANTED }
            .distinctUntilChanged()
            .collect { analytics.setCollectionEnabled(it) }
    }
}
