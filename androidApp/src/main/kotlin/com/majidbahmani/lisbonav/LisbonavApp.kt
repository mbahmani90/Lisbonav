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
    }
}
