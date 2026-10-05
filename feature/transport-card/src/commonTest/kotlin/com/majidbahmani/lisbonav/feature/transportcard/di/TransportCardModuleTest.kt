package com.majidbahmani.lisbonav.feature.transportcard.di

import com.majidbahmani.lisbonav.analytics.Analytics
import com.majidbahmani.lisbonav.feature.transportcard.data.nfc.CardTapSource
import com.majidbahmani.lisbonav.feature.transportcard.data.repository.CalypsoTransportCardReader
import com.majidbahmani.lisbonav.feature.transportcard.domain.repository.TransportCardReader
import com.majidbahmani.lisbonav.feature.transportcard.domain.usecase.ReadTransportCardUseCase
import com.majidbahmani.lisbonav.feature.transportcard.fake.FakeAnalytics
import com.majidbahmani.lisbonav.feature.transportcard.fake.FakeCardTapSource
import com.majidbahmani.lisbonav.feature.transportcard.presentation.viewmodel.TransportCardViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/** The feature's bindings with a fake tap source (the real one needs Android NFC) and Analytics. */
@OptIn(ExperimentalCoroutinesApi::class)
class TransportCardModuleTest {

    private val app = koinApplication {
        modules(
            transportCardModule,
            module {
                single<CardTapSource> { FakeCardTapSource() }
                single<Analytics> { FakeAnalytics() } // from the app (the platform's Firebase)
            },
        )
    }

    // The ViewModel starts coroutines in viewModelScope (Dispatchers.Main).
    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() {
        app.close()
        Dispatchers.resetMain()
    }

    @Test
    fun reader_resolvesToCalypsoImplementation_asSingle() {
        val reader = app.koin.get<TransportCardReader>()

        assertIs<CalypsoTransportCardReader>(reader)
        assertSame(reader, app.koin.get<TransportCardReader>())
    }

    @Test
    fun viewModel_resolves() {
        assertIs<TransportCardViewModel>(app.koin.get<TransportCardViewModel>())
    }

    @Test
    fun useCase_isNewInstancePerInjection() {
        assertNotSame(app.koin.get<ReadTransportCardUseCase>(), app.koin.get<ReadTransportCardUseCase>())
    }
}
