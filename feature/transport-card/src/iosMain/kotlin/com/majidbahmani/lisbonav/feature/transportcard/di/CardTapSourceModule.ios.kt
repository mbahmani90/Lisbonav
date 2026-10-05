package com.majidbahmani.lisbonav.feature.transportcard.di

import com.majidbahmani.calypso.nfc.NfcUnavailableException
import com.majidbahmani.lisbonav.feature.transportcard.data.nfc.CardTapSource
import kotlinx.coroutines.flow.flow
import org.koin.core.module.Module
import org.koin.dsl.module

actual val cardTapSourceModule: Module = module {
    // Core NFC isn't wired yet (needs an iPhone and a paid Apple developer account): report it as unsupported.
    single<CardTapSource> {
        CardTapSource { flow { throw NfcUnavailableException(NfcUnavailableException.Reason.NOT_SUPPORTED) } }
    }
}
