package com.majidbahmani.lisbonav.feature.consent.presentation.viewmodel

import com.majidbahmani.lisbonav.feature.consent.domain.model.AnalyticsConsent
import com.majidbahmani.lisbonav.feature.consent.domain.usecase.ApplyAnalyticsConsentUseCase
import com.majidbahmani.lisbonav.feature.consent.domain.usecase.ObserveConsentNeededUseCase
import com.majidbahmani.lisbonav.feature.consent.domain.usecase.SetAnalyticsConsentUseCase
import com.majidbahmani.lisbonav.feature.consent.fake.FakeAnalytics
import com.majidbahmani.lisbonav.feature.consent.fake.FakeConsentRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class ConsentViewModelTest {

    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private val analytics = FakeAnalytics()

    // The real use cases with a fake repository (doc 22).
    private fun viewModel(repository: FakeConsentRepository) = ConsentViewModel(
        observeConsentNeeded = ObserveConsentNeededUseCase(repository),
        applyConsent = ApplyAnalyticsConsentUseCase(repository, analytics),
        setConsent = SetAnalyticsConsentUseCase(repository),
    )

    /** The app showing the dialog's state. */
    private fun TestScope.show(viewModel: ConsentViewModel) {
        backgroundScope.launch { viewModel.showDialog.collect {} }
        runCurrent()
    }

    @Test
    fun firstLaunch_asks_andCollectsNothing() = runTest {
        val viewModel = viewModel(FakeConsentRepository())
        show(viewModel)

        assertTrue(viewModel.showDialog.value)
        assertEquals(listOf(false), analytics.collectionEnabled)
    }

    @Test
    fun allow_savesTheAnswer_closesTheDialog_andTurnsCollectionOn() = runTest {
        val repository = FakeConsentRepository()
        val viewModel = viewModel(repository)
        show(viewModel)

        viewModel.onAllow()
        runCurrent()

        assertEquals(AnalyticsConsent.GRANTED, repository.saved.value)
        assertFalse(viewModel.showDialog.value)
        assertEquals(listOf(false, true), analytics.collectionEnabled)
    }

    @Test
    fun dontAllow_savesTheAnswer_closesTheDialog_andKeepsCollectionOff() = runTest {
        val repository = FakeConsentRepository()
        val viewModel = viewModel(repository)
        show(viewModel)

        viewModel.onDeny()
        runCurrent()

        assertEquals(AnalyticsConsent.DENIED, repository.saved.value)
        assertFalse(viewModel.showDialog.value)
        assertEquals(listOf(false), analytics.collectionEnabled)
    }

    @Test
    fun alreadyAnswered_neverAsks() = runTest {
        val viewModel = viewModel(FakeConsentRepository(AnalyticsConsent.DENIED))

        assertFalse(viewModel.showDialog.value) // before the saved answer is read: no flash
        show(viewModel)
        assertFalse(viewModel.showDialog.value)
    }
}
