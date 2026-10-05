package com.majidbahmani.lisbonav.feature.consent.domain.usecase

import com.majidbahmani.lisbonav.feature.consent.domain.model.AnalyticsConsent
import com.majidbahmani.lisbonav.feature.consent.fake.FakeAnalytics
import com.majidbahmani.lisbonav.feature.consent.fake.FakeConsentRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class ApplyAnalyticsConsentUseCaseTest {

    private val analytics = FakeAnalytics()

    @Test
    fun noAnswerYet_keepsCollectionOff() = runTest {
        backgroundScope.launch { ApplyAnalyticsConsentUseCase(FakeConsentRepository(), analytics)() }
        runCurrent()

        assertEquals(listOf(false), analytics.collectionEnabled)
    }

    @Test
    fun followsTheAnswer_onlyAllowTurnsCollectionOn() = runTest {
        val repository = FakeConsentRepository()
        backgroundScope.launch { ApplyAnalyticsConsentUseCase(repository, analytics)() }
        runCurrent()

        repository.saved.value = AnalyticsConsent.GRANTED
        runCurrent()
        repository.saved.value = AnalyticsConsent.DENIED
        runCurrent()

        assertEquals(listOf(false, true, false), analytics.collectionEnabled)
    }

    @Test
    fun earlierAllow_turnsCollectionOnAtStart() = runTest {
        backgroundScope.launch {
            ApplyAnalyticsConsentUseCase(FakeConsentRepository(AnalyticsConsent.GRANTED), analytics)()
        }
        runCurrent()

        assertEquals(listOf(true), analytics.collectionEnabled)
    }
}
