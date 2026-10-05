package com.majidbahmani.lisbonav.feature.consent.data.repository

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.majidbahmani.lisbonav.feature.consent.domain.model.AnalyticsConsent
import com.majidbahmani.lisbonav.feature.consent.fake.createTestDataStore
import com.majidbahmani.lisbonav.feature.consent.fake.deleteDataStoreFile
import com.majidbahmani.lisbonav.feature.consent.fake.newDataStorePath
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** With a real DataStore on a temporary file (doc 28). */
class DataStoreConsentRepositoryTest {

    private val path = newDataStorePath()

    @AfterTest
    fun tearDown() = deleteDataStoreFile(path)

    private fun TestScope.dataStore() = createTestDataStore(backgroundScope, path)

    @Test
    fun nothingSaved_isNoAnswer() = runTest {
        assertNull(DataStoreConsentRepository(dataStore()).analyticsConsent.first())
    }

    @Test
    fun savedAnswer_isReadBack_andCanChange() = runTest {
        val repository = DataStoreConsentRepository(dataStore())

        repository.setAnalyticsConsent(AnalyticsConsent.GRANTED)
        assertEquals(AnalyticsConsent.GRANTED, repository.analyticsConsent.first())

        repository.setAnalyticsConsent(AnalyticsConsent.DENIED)
        assertEquals(AnalyticsConsent.DENIED, repository.analyticsConsent.first())
    }

    @Test
    fun answer_isStoredByName() = runTest {
        val dataStore = dataStore()
        DataStoreConsentRepository(dataStore).setAnalyticsConsent(AnalyticsConsent.DENIED)

        assertEquals("DENIED", dataStore.data.first()[stringPreferencesKey("analytics_consent")])
    }

    @Test
    fun unknownSavedValue_isNoAnswer() = runTest {
        val dataStore = dataStore()
        dataStore.edit { it[stringPreferencesKey("analytics_consent")] = "MAYBE" }

        assertNull(DataStoreConsentRepository(dataStore).analyticsConsent.first())
    }
}
