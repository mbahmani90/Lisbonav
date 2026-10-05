package com.majidbahmani.lisbonav.core.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.majidbahmani.lisbonav.core.datastore.SETTINGS_FILE_NAME
import com.majidbahmani.lisbonav.core.datastore.createDataStore
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual val dataStoreModule: Module = module {
    single<DataStore<Preferences>> { createDataStore { settingsFilePath() } }
}

/** Application Support: the app's own files, not shown to the user (unlike Documents). */
@OptIn(ExperimentalForeignApi::class)
private fun settingsFilePath(): String {
    val directory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true, // doesn't exist on a fresh install
        error = null,
    )
    return requireNotNull(directory?.path) { "No Application Support directory" } + "/" + SETTINGS_FILE_NAME
}
