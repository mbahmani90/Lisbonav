package com.majidbahmani.lisbonav.di

import com.majidbahmani.lisbonav.data.remote.CarrisMetropolitanaApi
import com.majidbahmani.lisbonav.data.remote.KtorCarrisMetropolitanaApi
import com.majidbahmani.lisbonav.data.remote.createHttpClient
import io.ktor.client.HttpClient
import org.koin.dsl.module
import org.koin.dsl.onClose

val networkModule = module {
    // One client for the app: it owns the connection pool. The engine comes from platformModule.
    single<HttpClient> { createHttpClient(engine = get()) } onClose { it?.close() }

    single<CarrisMetropolitanaApi> { KtorCarrisMetropolitanaApi(client = get()) }
}
