package com.majidbahmani.lisbonav.di

import org.koin.core.module.Module

/** Platform-specific bindings, e.g. the Ktor engine (OkHttp on Android, Darwin on iOS). */
expect val platformModule: Module
