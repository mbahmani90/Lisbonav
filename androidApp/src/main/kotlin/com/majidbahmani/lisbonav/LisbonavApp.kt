package com.majidbahmani.lisbonav

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.analytics
import com.majidbahmani.lisbonav.analytics.Analytics
import com.majidbahmani.lisbonav.analytics.FirebaseAndroidAnalytics
import com.majidbahmani.lisbonav.analytics.NoOpAnalytics
import com.majidbahmani.lisbonav.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class LisbonavApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(analytics = createAnalytics()) {
            androidLogger()
            androidContext(this@LisbonavApp)
        }
    }

    /** Firebase when google-services.json was there at build time, otherwise nothing is logged. */
    private fun createAnalytics(): Analytics =
        if (FirebaseApp.getApps(this).isNotEmpty()) FirebaseAndroidAnalytics(Firebase.analytics) else NoOpAnalytics
}
