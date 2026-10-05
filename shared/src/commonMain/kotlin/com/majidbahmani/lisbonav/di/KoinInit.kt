package com.majidbahmani.lisbonav.di

import com.majidbahmani.lisbonav.core.di.httpEngineModule
import com.majidbahmani.lisbonav.core.di.networkModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/** Every module of the app; also used by the module test. */
internal val appModules = listOf(
    // :core
    httpEngineModule,
    networkModule,
    // this app
    apiModule,
    repositoryModule,
    useCaseModule,
    viewModelModule,
)

/**
 * Starts Koin once per process. Android calls it from `LisbonavApp` (adding `androidContext`),
 * iOS from `iOSApp.swift` via [initKoinIos].
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication = startKoin {
    appDeclaration()
    modules(appModules)
}
