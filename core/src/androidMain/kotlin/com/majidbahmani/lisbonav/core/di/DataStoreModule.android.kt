package com.majidbahmani.lisbonav.core.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.majidbahmani.lisbonav.core.datastore.SETTINGS_FILE_NAME
import com.majidbahmani.lisbonav.core.datastore.createDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val dataStoreModule: Module = module {
    single<DataStore<Preferences>> {
        val context = androidContext()
        createDataStore { context.filesDir.resolve(SETTINGS_FILE_NAME).absolutePath }
    }
}
