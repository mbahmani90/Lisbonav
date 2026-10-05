package com.majidbahmani.lisbonav.core.di

import com.majidbahmani.lisbonav.core.network.createHttpClient
import io.ktor.client.HttpClient
import org.koin.dsl.module
import org.koin.dsl.onClose

/** The shared HttpClient. Needs [httpEngineModule] for the engine. */
val networkModule = module {
    // One client for the app: it owns the connection pool.
    single<HttpClient> { createHttpClient(engine = get()) } onClose { it?.close() }
}
