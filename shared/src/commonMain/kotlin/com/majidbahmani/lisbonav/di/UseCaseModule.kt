package com.majidbahmani.lisbonav.di

import com.majidbahmani.lisbonav.domain.usecase.GetVehiclesUseCase
import org.koin.dsl.module

val useCaseModule = module {
    // Stateless and cheap: a new instance per injection; the repository it uses is a single.
    factory { GetVehiclesUseCase(repository = get()) }
}
