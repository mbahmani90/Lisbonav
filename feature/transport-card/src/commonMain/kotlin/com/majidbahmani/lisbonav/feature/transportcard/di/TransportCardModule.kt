package com.majidbahmani.lisbonav.feature.transportcard.di

import com.majidbahmani.lisbonav.feature.transportcard.data.repository.CalypsoTransportCardReader
import com.majidbahmani.lisbonav.feature.transportcard.domain.repository.TransportCardReader
import com.majidbahmani.lisbonav.feature.transportcard.domain.usecase.ReadTransportCardUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

/** Everything the transport card feature needs. Add [cardTapSourceModule] for the platform's NFC. */
val transportCardModule = module {
    single<TransportCardReader> { CalypsoTransportCardReader(cardTaps = get()) }
    // Stateless and cheap: a new instance per injection; the reader it uses is a single.
    factory { ReadTransportCardUseCase(reader = get()) }
}

/** Where tapped cards come from: NFC reader mode on Android, not supported (yet) on iOS. */
expect val cardTapSourceModule: Module
