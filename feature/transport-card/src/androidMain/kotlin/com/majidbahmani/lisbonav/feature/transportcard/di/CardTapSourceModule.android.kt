package com.majidbahmani.lisbonav.feature.transportcard.di

import com.majidbahmani.calypso.nfc.NfcTagReader
import com.majidbahmani.lisbonav.feature.transportcard.data.nfc.AndroidCardTapSource
import com.majidbahmani.lisbonav.feature.transportcard.data.nfc.CardTapSource
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.dsl.module

actual val cardTapSourceModule: Module = module {
    // createdAtStart: NfcTagReader must exist before the first Activity resumes, to see it resume
    // (it follows activities through lifecycle callbacks). Koin creates it in Application.onCreate.
    single<CardTapSource>(createdAtStart = true) { AndroidCardTapSource(NfcTagReader(androidApplication())) }
}
