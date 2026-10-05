package com.majidbahmani.lisbonav.feature.consent.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.majidbahmani.lisbonav.analytics.Analytics
import com.majidbahmani.lisbonav.feature.consent.data.repository.DataStoreConsentRepository
import com.majidbahmani.lisbonav.feature.consent.domain.repository.ConsentRepository
import com.majidbahmani.lisbonav.feature.consent.fake.FakeAnalytics
import com.majidbahmani.lisbonav.feature.consent.fake.createTestDataStore
import com.majidbahmani.lisbonav.feature.consent.fake.deleteDataStoreFile
import com.majidbahmani.lisbonav.feature.consent.fake.newDataStorePath
import com.majidbahmani.lisbonav.feature.consent.presentation.viewmodel.ConsentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

/** The feature's bindings, with a test DataStore (from :core in the app) and a fake Analytics. */
@OptIn(ExperimentalCoroutinesApi::class)
class ConsentModuleTest {

    private val path = newDataStorePath()
    private lateinit var scope: CoroutineScope

    private val app by lazy {
        koinApplication {
            modules(
                consentModule,
                module {
                    single<DataStore<Preferences>> { createTestDataStore(scope, path) }
                    single<Analytics> { FakeAnalytics() }
                },
            )
        }
    }

    // The ViewModel starts coroutines in viewModelScope (Dispatchers.Main); the DataStore runs in [scope].
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        scope = MainScope()
    }

    @AfterTest
    fun tearDown() {
        app.close()
        scope.cancel()
        Dispatchers.resetMain()
        deleteDataStoreFile(path)
    }

    @Test
    fun repository_resolvesToDataStoreImplementation_asSingle() {
        val repository = app.koin.get<ConsentRepository>()

        assertIs<DataStoreConsentRepository>(repository)
        assertSame(repository, app.koin.get<ConsentRepository>())
    }

    @Test
    fun viewModel_resolves() {
        assertIs<ConsentViewModel>(app.koin.get<ConsentViewModel>())
    }
}
