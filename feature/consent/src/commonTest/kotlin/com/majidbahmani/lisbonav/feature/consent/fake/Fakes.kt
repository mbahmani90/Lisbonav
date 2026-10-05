package com.majidbahmani.lisbonav.feature.consent.fake

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.majidbahmani.lisbonav.analytics.Analytics
import com.majidbahmani.lisbonav.analytics.AnalyticsEvent
import com.majidbahmani.lisbonav.feature.consent.domain.model.AnalyticsConsent
import com.majidbahmani.lisbonav.feature.consent.domain.repository.ConsentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import okio.FileSystem
import okio.Path
import kotlin.random.Random

/** In memory; [saved] is what the user answered (null: not yet). */
class FakeConsentRepository(answer: AnalyticsConsent? = null) : ConsentRepository {
    val saved = MutableStateFlow(answer)
    override val analyticsConsent = saved
    override suspend fun setAnalyticsConsent(consent: AnalyticsConsent) {
        saved.value = consent
    }
}

/** Records every setCollectionEnabled() call, in order. */
class FakeAnalytics : Analytics {
    val collectionEnabled = mutableListOf<Boolean>()
    override fun log(event: AnalyticsEvent) = Unit
    override fun setCollectionEnabled(enabled: Boolean) {
        collectionEnabled += enabled
    }
}

/** A real DataStore on a new temporary file; delete it with [deleteDataStoreFile]. */
fun createTestDataStore(scope: CoroutineScope, path: Path = newDataStorePath()): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(scope = scope, produceFile = { path })

fun newDataStorePath(): Path =
    FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "consent-test-${Random.nextLong().toULong()}.preferences_pb"

fun deleteDataStoreFile(path: Path) = FileSystem.SYSTEM.delete(path, mustExist = false)
