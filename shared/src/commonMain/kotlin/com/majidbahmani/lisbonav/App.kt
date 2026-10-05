package com.majidbahmani.lisbonav

import androidx.compose.runtime.Composable
import com.majidbahmani.lisbonav.presentation.ui.map.VehicleMapRoute
import com.majidbahmani.lisbonav.systemdesign.theme.LisbonavTheme

@Composable
fun App() {
    LisbonavTheme {
        VehicleMapRoute()
    }
}
