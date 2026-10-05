package com.majidbahmani.lisbonav

import android.app.Application
import com.majidbahmani.lisbonav.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class LisbonavApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@LisbonavApp)
        }
        // Debug builds: log a raw dump of any tapped transit card. Release builds: does nothing.
        DebugTools.install(this)
    }
}
