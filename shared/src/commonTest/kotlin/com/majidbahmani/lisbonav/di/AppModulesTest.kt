package com.majidbahmani.lisbonav.di

import com.majidbahmani.lisbonav.data.remote.CarrisMetropolitanaApi
import com.majidbahmani.lisbonav.data.remote.KtorCarrisMetropolitanaApi
import com.majidbahmani.lisbonav.data.repository.VehicleRepositoryImpl
import com.majidbahmani.lisbonav.domain.repository.VehicleRepository
import com.majidbahmani.lisbonav.domain.usecase.GetVehiclesUseCase
import com.majidbahmani.lisbonav.presentation.viewmodel.VehicleMapViewModel
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertSame

/** Resolves the real module graph on each platform (OkHttp on Android, Darwin on iOS); no network calls. */
@OptIn(ExperimentalCoroutinesApi::class)
class AppModulesTest {

    // A local KoinApplication, not startKoin(): tests don't touch the global Koin instance.
    private val app: KoinApplication = koinApplication { modules(appModules) }

    // ViewModels start coroutines in viewModelScope (Dispatchers.Main), which tests don't have.
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

    @Test
    fun httpClient_isSingleton() {
        assertSame(app.koin.get<HttpClient>(), app.koin.get<HttpClient>())
    }
}
