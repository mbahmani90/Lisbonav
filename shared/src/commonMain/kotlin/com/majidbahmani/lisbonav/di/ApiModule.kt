package com.majidbahmani.lisbonav.di

import com.majidbahmani.lisbonav.data.remote.CarrisMetropolitanaApi
import com.majidbahmani.lisbonav.data.remote.KtorCarrisMetropolitanaApi
import org.koin.dsl.module

/** The Carris Metropolitana API on top of the shared HttpClient from :core. */
val apiModule = module {
    single<CarrisMetropolitanaApi> { KtorCarrisMetropolitanaApi(client = get()) }
}
