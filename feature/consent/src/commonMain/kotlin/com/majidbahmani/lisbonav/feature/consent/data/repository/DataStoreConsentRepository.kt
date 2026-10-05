package com.majidbahmani.lisbonav.feature.consent.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.majidbahmani.lisbonav.feature.consent.domain.model.AnalyticsConsent
import com.majidbahmani.lisbonav.feature.consent.domain.repository.ConsentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import okio.IOException

/** The answer in the app's settings DataStore, stored by name (enum order can change). */
internal class DataStoreConsentRepository(
    private val dataStore: DataStore<Preferences>,
) : ConsentRepository {

    override val analyticsConsent: Flow<AnalyticsConsent?> = dataStore.data
        // Unreadable file → no answer: the user is asked again and collection stays off.
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            prefs[ANALYTICS_CONSENT]?.let { saved -> AnalyticsConsent.entries.firstOrNull { it.name == saved } }
        }
        .distinctUntilChanged() // other settings changing shouldn't re-emit

    override suspend fun setAnalyticsConsent(consent: AnalyticsConsent) {
        dataStore.edit { it[ANALYTICS_CONSENT] = consent.name }
    }

    private companion object {
        val ANALYTICS_CONSENT = stringPreferencesKey("analytics_consent")
    }
}
