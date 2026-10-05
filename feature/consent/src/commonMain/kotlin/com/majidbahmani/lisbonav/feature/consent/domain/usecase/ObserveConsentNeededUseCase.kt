package com.majidbahmani.lisbonav.feature.consent.domain.usecase

import com.majidbahmani.lisbonav.feature.consent.domain.repository.ConsentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Whether the user still has to be asked: only until they answer, whatever the answer. */
class ObserveConsentNeededUseCase(private val repository: ConsentRepository) {
    operator fun invoke(): Flow<Boolean> = repository.analyticsConsent
        .map { it == null }
        .distinctUntilChanged()
}
