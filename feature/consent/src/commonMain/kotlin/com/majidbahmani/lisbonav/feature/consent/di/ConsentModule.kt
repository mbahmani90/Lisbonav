package com.majidbahmani.lisbonav.feature.consent.di

import com.majidbahmani.lisbonav.feature.consent.data.repository.DataStoreConsentRepository
import com.majidbahmani.lisbonav.feature.consent.domain.repository.ConsentRepository
import com.majidbahmani.lisbonav.feature.consent.domain.usecase.ApplyAnalyticsConsentUseCase
import com.majidbahmani.lisbonav.feature.consent.domain.usecase.ObserveConsentNeededUseCase
import com.majidbahmani.lisbonav.feature.consent.domain.usecase.SetAnalyticsConsentUseCase
import com.majidbahmani.lisbonav.feature.consent.presentation.viewmodel.ConsentViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Everything the consent feature needs. Also needs the settings DataStore (`dataStoreModule` in
 * :core) and the platform app's Analytics.
 */
val consentModule = module {
    single<ConsentRepository> { DataStoreConsentRepository(dataStore = get()) }
    // Stateless and cheap: a new instance per injection.
    factory { ObserveConsentNeededUseCase(repository = get()) }
    factory { ApplyAnalyticsConsentUseCase(repository = get(), analytics = get()) }
    factory { SetAnalyticsConsentUseCase(repository = get()) }
    viewModel { ConsentViewModel(observeConsentNeeded = get(), applyConsent = get(), setConsent = get()) }
}
