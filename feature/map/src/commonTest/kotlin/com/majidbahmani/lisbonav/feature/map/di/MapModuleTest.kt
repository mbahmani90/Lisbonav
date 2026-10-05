package com.majidbahmani.lisbonav.feature.map.di

import com.majidbahmani.lisbonav.analytics.Analytics
import com.majidbahmani.lisbonav.core.di.httpEngineModule
import com.majidbahmani.lisbonav.core.di.networkModule
import com.majidbahmani.lisbonav.feature.map.data.remote.CarrisMetropolitanaApi
import com.majidbahmani.lisbonav.feature.map.data.remote.KtorCarrisMetropolitanaApi
import com.majidbahmani.lisbonav.feature.map.data.repository.VehicleRepositoryImpl
import com.majidbahmani.lisbonav.feature.map.domain.repository.VehicleRepository
import com.majidbahmani.lisbonav.feature.map.domain.usecase.GetVehiclesUseCase
import com.majidbahmani.lisbonav.feature.map.fake.FakeAnalytics
import com.majidbahmani.lisbonav.feature.map.presentation.viewmodel.VehicleMapViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/** The feature's bindings, including its internal classes, on top of :core; no network calls. */
@OptIn(ExperimentalCoroutinesApi::class)
class MapModuleTest {

    // Analytics comes from the app (the platform's Firebase); a fake here.
    private val app = koinApplication {
        modules(httpEngineModule, networkModule, mapModule, module { single<Analytics> { FakeAnalytics() } })
    }

    // The ViewModel starts coroutines in viewModelScope (Dispatchers.Main).
    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() {
        app.close()
        Dispatchers.resetMain()
    }

    @Test
    fun api_resolvesToKtorImplementation() {
        assertIs<KtorCarrisMetropolitanaApi>(app.koin.get<CarrisMetropolitanaApi>())
    }

    @Test
    fun vehicleRepository_resolvesToImplementation() {
        assertIs<VehicleRepositoryImpl>(app.koin.get<VehicleRepository>())
    }

    @Test
    fun getVehiclesUseCase_isNewInstancePerInjection() {
        assertNotSame(app.koin.get<GetVehiclesUseCase>(), app.koin.get<GetVehiclesUseCase>())
    }

    @Test
    fun vehicleMapViewModel_resolves() {
        assertIs<VehicleMapViewModel>(app.koin.get<VehicleMapViewModel>())
    }
}
