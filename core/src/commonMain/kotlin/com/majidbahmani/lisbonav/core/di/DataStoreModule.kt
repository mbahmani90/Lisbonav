package com.majidbahmani.lisbonav.core.di

import org.koin.core.module.Module

/**
 * The app's single settings `DataStore<Preferences>` (one instance per file, so a Koin single).
 * The file lives in the app's private storage: `filesDir` on Android, Application Support on iOS.
 */
expect val dataStoreModule: Module
