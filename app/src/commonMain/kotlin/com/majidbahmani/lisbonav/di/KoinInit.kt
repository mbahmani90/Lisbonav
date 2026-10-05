package com.majidbahmani.lisbonav.di

import com.majidbahmani.lisbonav.core.di.dataStoreModule
import com.majidbahmani.lisbonav.core.di.httpEngineModule
import com.majidbahmani.lisbonav.core.di.networkModule
import com.majidbahmani.lisbonav.feature.consent.di.consentModule
import com.majidbahmani.lisbonav.feature.map.di.mapModule
import com.majidbahmani.lisbonav.feature.transportcard.di.cardTapSourceModule
import com.majidbahmani.lisbonav.feature.transportcard.di.transportCardModule
import com.majidbahmani.lisbonav.analytics.Analytics
import com.majidbahmani.lisbonav.analytics.NoOpAnalytics
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

/** Every module of the app; also used by the module test. */
internal val appModules = listOf(
    // :core
    httpEngineModule,
    networkModule,
    dataStoreModule,
    // features
    mapModule,
    transportCardModule,
    cardTapSourceModule,
    consentModule,
)

/**
 * Binds the platform app's [Analytics]: the instance is created by the platform app (it needs the
 * platform's SDK), so it comes in as a value instead of being built here.
 */
internal fun analyticsModule(analytics: Analytics): Module = module {
    single<Analytics> { analytics }
}

/**
 * Starts Koin once per process. Android calls it from `LisbonavApp` (adding `androidContext`),
 * iOS from `iOSApp.swift` via [initKoinIos].
 *
 * @param analytics the platform app's implementation; [NoOpAnalytics] until Firebase is added.
 */
fun initKoin(
    analytics: Analytics = NoOpAnalytics,
    appDeclaration: KoinAppDeclaration = {},
): KoinApplication = startKoin {
    appDeclaration()
    modules(listOf(analyticsModule(analytics)) + appModules)
}
