package com.majidbahmani.lisbonav.core.di

import org.koin.core.module.Module

/** The Ktor engine for this platform: OkHttp on Android, Darwin on iOS. */
expect val httpEngineModule: Module
