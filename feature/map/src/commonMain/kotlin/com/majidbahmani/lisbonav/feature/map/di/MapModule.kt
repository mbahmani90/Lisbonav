package com.majidbahmani.lisbonav.feature.map.di

import com.majidbahmani.lisbonav.feature.map.data.remote.CarrisMetropolitanaApi
import com.majidbahmani.lisbonav.feature.map.data.remote.KtorCarrisMetropolitanaApi
import com.majidbahmani.lisbonav.feature.map.data.repository.VehicleRepositoryImpl
import com.majidbahmani.lisbonav.feature.map.domain.repository.VehicleRepository
import com.majidbahmani.lisbonav.feature.map.domain.usecase.GetVehiclesUseCase
import com.majidbahmani.lisbonav.feature.map.presentation.viewmodel.VehicleMapViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Everything the map feature needs. The data classes are internal: only this module creates them.
 * Needs the HttpClient from :core (httpEngineModule + networkModule).
 */
val mapModule = module {
    single<CarrisMetropolitanaApi> { KtorCarrisMetropolitanaApi(client = get()) }
    single<VehicleRepository> { VehicleRepositoryImpl(api = get()) }
    // Stateless and cheap: a new instance per injection; the repository it uses is a single.
    factory { GetVehiclesUseCase(repository = get()) }
    viewModel { VehicleMapViewModel(getVehicles = get()) }
}
