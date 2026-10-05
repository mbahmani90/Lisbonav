package com.majidbahmani.lisbonav.di

import com.majidbahmani.lisbonav.analytics.Analytics
import com.majidbahmani.lisbonav.analytics.AnalyticsEvent
import com.majidbahmani.lisbonav.feature.map.presentation.viewmodel.VehicleMapViewModel
import io.ktor.client.HttpClient
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication

/**
 * The whole app graph (:core + every feature) resolves on each platform (OkHttp on Android,
 * Darwin on iOS); no network calls. Each feature tests its own bindings in detail.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppModulesTest {

    // A local KoinApplication, not startKoin(): tests don't touch the global Koin instance.
    // No eager instances: the Android NFC tag reader needs a real Application, which host tests don't have.
    private val analytics = FakeAnalytics()

    private val app: KoinApplication = koinApplication(createEagerInstances = false) {
        modules(listOf(analyticsModule(analytics)) + appModules)
    }

    // ViewModels start coroutines in viewModelScope (Dispatchers.Main), which tests don't have.
    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() {
        app.close()
        Dispatchers.resetMain()
    }

    @Test
    fun mapScreen_viewModelResolves_withTheWholeChain() {
        // Needs mapModule (use case, repository, API) and :core (HttpClient, engine).
        assertIs<VehicleMapViewModel>(app.koin.get<VehicleMapViewModel>())
    }

    @Test
    fun analytics_isTheInstanceThePlatformAppPassedIn() {
        assertSame<Analytics>(analytics, app.koin.get<Analytics>())
    }

    @Test
    fun httpClient_isSingleton() {
        assertSame(app.koin.get<HttpClient>(), app.koin.get<HttpClient>())
    }
}

/** Stands in for the platform app's implementation (Firebase on the devices). */
private class FakeAnalytics : Analytics {
    override fun log(event: AnalyticsEvent) = Unit
    override fun setCollectionEnabled(enabled: Boolean) = Unit
}
