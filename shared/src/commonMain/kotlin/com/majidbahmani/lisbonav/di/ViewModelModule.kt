package com.majidbahmani.lisbonav.di

import com.majidbahmani.lisbonav.presentation.viewmodel.VehicleMapViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { VehicleMapViewModel(getVehicles = get()) }
}
