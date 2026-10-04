package com.majidbahmani.lisbonav.di

import com.majidbahmani.lisbonav.data.repository.VehicleRepositoryImpl
import com.majidbahmani.lisbonav.domain.repository.VehicleRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<VehicleRepository> { VehicleRepositoryImpl(api = get()) }
}
