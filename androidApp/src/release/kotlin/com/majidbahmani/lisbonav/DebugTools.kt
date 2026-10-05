package com.majidbahmani.lisbonav

import android.app.Application

/** Release builds: no debug tools (see src/debug/DebugTools.kt). */
object DebugTools {
    fun install(application: Application) = Unit
}
