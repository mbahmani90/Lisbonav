package com.majidbahmani.lisbonav.feature.transportcard.di

import com.majidbahmani.lisbonav.feature.transportcard.data.nfc.CardTapSource
import com.majidbahmani.lisbonav.feature.transportcard.data.repository.CalypsoTransportCardReader
import com.majidbahmani.lisbonav.feature.transportcard.domain.repository.TransportCardReader
import com.majidbahmani.lisbonav.feature.transportcard.fake.FakeCardTapSource
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

/** The feature's bindings with a fake tap source (the real one needs Android NFC). */
class TransportCardModuleTest {

    private val app = koinApplication {
        modules(transportCardModule, module { single<CardTapSource> { FakeCardTapSource() } })
    }

    @AfterTest
    fun tearDown() = app.close()

    @Test
    fun reader_resolvesToCalypsoImplementation_asSingle() {
        val reader = app.koin.get<TransportCardReader>()

        assertIs<CalypsoTransportCardReader>(reader)
        assertSame(reader, app.koin.get<TransportCardReader>())
    }
}
