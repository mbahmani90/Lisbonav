package com.majidbahmani.lisbonav.feature.consent.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.majidbahmani.lisbonav.feature.consent.domain.model.AnalyticsConsent
import com.majidbahmani.lisbonav.feature.consent.domain.usecase.ApplyAnalyticsConsentUseCase
import com.majidbahmani.lisbonav.feature.consent.domain.usecase.ObserveConsentNeededUseCase
import com.majidbahmani.lisbonav.feature.consent.domain.usecase.SetAnalyticsConsentUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * The consent dialog, shown by the app over its first screen.
 *
 * - [showDialog] is true until the user answers; false until the saved answer is read, so the
 *   dialog never flashes for users who already answered.
 * - The saved answer is applied to Analytics for as long as the app's UI exists (this ViewModel
 *   lives in the app's root, not in a screen).
 */
class ConsentViewModel(
    observeConsentNeeded: ObserveConsentNeededUseCase,
    applyConsent: ApplyAnalyticsConsentUseCase,
    private val setConsent: SetAnalyticsConsentUseCase,
) : ViewModel() {

    val showDialog: StateFlow<Boolean> = observeConsentNeeded().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = false,
    )

    init {
        viewModelScope.launch { applyConsent() }
    }

    fun onAllow() = answer(AnalyticsConsent.GRANTED)

    fun onDeny() = answer(AnalyticsConsent.DENIED)

    private fun answer(consent: AnalyticsConsent) {
        viewModelScope.launch { setConsent(consent) }
    }
}
