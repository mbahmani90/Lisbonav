package com.majidbahmani.lisbonav.feature.consent.domain.usecase

import com.majidbahmani.lisbonav.feature.consent.domain.model.AnalyticsConsent
import com.majidbahmani.lisbonav.feature.consent.domain.repository.ConsentRepository

/** Saves the user's answer; [ApplyAnalyticsConsentUseCase] picks it up from storage. */
class SetAnalyticsConsentUseCase(private val repository: ConsentRepository) {
    suspend operator fun invoke(consent: AnalyticsConsent) = repository.setAnalyticsConsent(consent)
}
