package com.majidbahmani.lisbonav.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

/** The app's settings file (user choices such as analytics consent). Must end with `.preferences_pb`. */
internal const val SETTINGS_FILE_NAME = "settings.preferences_pb"

/**
 * A Preferences DataStore stored at [producePath] (absolute path, platform-specific).
 * Create one per file and process: a second instance for the same file throws.
 */
fun createDataStore(producePath: () -> String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { producePath().toPath() })
